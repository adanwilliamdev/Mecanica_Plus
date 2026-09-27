import { useQuery } from "@tanstack/react-query"

import { apiFetch } from "@/lib/api-client"
import { queryKeys } from "@/lib/query-keys"
import { useAppMutation } from "@/lib/use-app-mutation"
import type { ServiceOrder, ServiceOrderItemRequest, ServiceOrderRequest, ServiceOrderStatus } from "@/types"

/** MECHANIC vê só as OS atribuídas a ele (GET /service-orders/mine); os demais veem todas. */
export const useServiceOrders = (onlyMine: boolean) =>
  useQuery({
    queryKey: [...queryKeys.serviceOrders, onlyMine ? "mine" : "all"],
    queryFn: () => apiFetch<ServiceOrder[]>(onlyMine ? "/service-orders/mine" : "/service-orders"),
  })

const invalidate = [queryKeys.serviceOrders, queryKeys.dashboard]

export const useCreateServiceOrder = () =>
  useAppMutation({
    mutationFn: (data: ServiceOrderRequest) => apiFetch<ServiceOrder>("/service-orders", { method: "POST", body: data }),
    invalidate,
    success: "Ordem de serviço criada com sucesso",
    error: "Erro ao criar ordem de serviço",
  })

export const useUpdateServiceOrderStatus = () =>
  useAppMutation({
    mutationFn: ({ id, status }: { id: string; status: ServiceOrderStatus }) =>
      apiFetch<ServiceOrder>(`/service-orders/${id}/status`, { method: "PATCH", body: { status } }),
    invalidate,
    success: "Status atualizado com sucesso",
    error: "Erro ao atualizar status",
    invalidateOnError: true,
  })

// Peças usadas mexem no estoque, então a lista de peças também precisa recarregar.
const invalidateWithParts = [...invalidate, queryKeys.parts]

export const useAddServiceOrderItem = () =>
  useAppMutation({
    mutationFn: ({ orderId, data }: { orderId: string; data: ServiceOrderItemRequest }) =>
      apiFetch<ServiceOrder>(`/service-orders/${orderId}/items`, { method: "POST", body: data }),
    invalidate: invalidateWithParts,
    success: "Peça adicionada à OS",
    error: "Erro ao adicionar peça",
  })

export const useRemoveServiceOrderItem = () =>
  useAppMutation({
    mutationFn: ({ orderId, itemId }: { orderId: string; itemId: string }) =>
      apiFetch<ServiceOrder>(`/service-orders/${orderId}/items/${itemId}`, { method: "DELETE" }),
    invalidate: invalidateWithParts,
    success: "Peça removida da OS",
    error: "Erro ao remover peça",
  })

export const useUpdateLaborAmount = () =>
  useAppMutation({
    mutationFn: ({ orderId, laborAmount }: { orderId: string; laborAmount: number }) =>
      apiFetch<ServiceOrder>(`/service-orders/${orderId}/labor`, { method: "PATCH", body: { laborAmount } }),
    invalidate,
    success: "Mão de obra atualizada",
    error: "Erro ao atualizar mão de obra",
  })
