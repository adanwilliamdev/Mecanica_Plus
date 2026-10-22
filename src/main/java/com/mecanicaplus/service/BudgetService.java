package com.mecanicaplus.service;

import com.mecanicaplus.domain.Budget;
import com.mecanicaplus.domain.BudgetStatus;
import com.mecanicaplus.dto.BudgetRequest;
import com.mecanicaplus.dto.BudgetResponse;
import com.mecanicaplus.exception.BusinessException;
import com.mecanicaplus.exception.NotFoundException;
import com.mecanicaplus.repository.BudgetRepository;
import com.mecanicaplus.repository.ServiceOrderRepository;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgets;
    private final ServiceOrderRepository orders;
    private final ClientService clientService;
    private final VehicleService vehicleService;

    public BudgetService(
            BudgetRepository budgets,
            ServiceOrderRepository orders,
            ClientService clientService,
            VehicleService vehicleService) {
        this.budgets = budgets;
        this.orders = orders;
        this.clientService = clientService;
        this.vehicleService = vehicleService;
    }

    public Budget getBudget(UUID id) {
        return budgets.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public BudgetResponse getResponse(UUID id) {
        return BudgetResponse.from(getBudget(id));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list() {
        return toResponses(budgets.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listByClient(UUID clientId) {
        return toResponses(budgets.findByClientIdOrderByCreatedAtDesc(clientId));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listByStatus(BudgetStatus status) {
        return toResponses(budgets.findByStatusOrderByCreatedAtDesc(status));
    }

    public BudgetResponse create(BudgetRequest data) {
        var client = clientService.getClient(data.clientId());
        var vehicle = vehicleService.getVehicle(data.vehicleId());
        if (data.serviceOrderId() != null && !orders.existsById(data.serviceOrderId())) {
            throw new NotFoundException("Ordem de serviço não encontrada com ID: " + data.serviceOrderId());
        }

        Budget budget = new Budget();
        budget.setBudgetNumber(nextBudgetNumber());
        budget.setClient(client);
        budget.setVehicle(vehicle);
        budget.setServiceOrderId(data.serviceOrderId());
        budget.setDescription(Texts.blankToNull(data.description()));
        budget.setTotalAmount(data.totalAmount());
        budget.setValidUntil(data.validUntil());
        budget.setStatus(BudgetStatus.PENDENTE);
        budgets.saveAndFlush(budget);
        return BudgetResponse.from(budget);
    }

    public BudgetResponse approve(UUID id) {
        return decide(id, BudgetStatus.APROVADO, "aprovados");
    }

    public BudgetResponse reject(UUID id) {
        return decide(id, BudgetStatus.RECUSADO, "recusados");
    }

    private BudgetResponse decide(UUID id, BudgetStatus newStatus, String verb) {
        Budget budget = getBudget(id);
        if (budget.getStatus() != BudgetStatus.PENDENTE) {
            throw new BusinessException("Apenas orçamentos pendentes podem ser " + verb);
        }
        budget.setStatus(newStatus);
        budgets.saveAndFlush(budget);
        return BudgetResponse.from(budget);
    }

    private String nextBudgetNumber() {
        long seq = budgets.nextBudgetSequence();
        return String.format("BUD%d%06d", Year.now(ZoneOffset.UTC).getValue(), seq);
    }

    private static List<BudgetResponse> toResponses(List<Budget> list) {
        return list.stream().map(BudgetResponse::from).toList();
    }
}
