"use client"

import { useState } from "react"
import { Plus } from "lucide-react"

import { ConfirmDialog } from "@/components/confirm-dialog"
import { DataTable, type Column, type RowAction } from "@/components/data-table"
import { PageHeader } from "@/components/page-header"
import { Button } from "@/components/ui/button"
import { useHasRole } from "@/features/auth/auth-provider"
import { useServiceOrders, useUpdateServiceOrderStatus } from "@/features/service-orders/queries"
import { ServiceOrderBillingDialog } from "@/features/service-orders/service-order-billing-dialog"
import { ServiceOrderForm } from "@/features/service-orders/service-order-form"
import { StatusBadge } from "@/features/service-orders/status-badge"
import { isTerminal, nextStatus, STATUS_LABELS } from "@/features/service-orders/status"
import { formatCurrency } from "@/lib/format"
import type { ServiceOrder } from "@/types"

const columns: Column<ServiceOrder>[] = [
  { header: "Número", cell: (o) => <span className="font-mono text-graphite-800">{o.orderNumber}</span> },
  { header: "Cliente", cell: (o) => o.clientName },
  { header: "Veículo", cell: (o) => o.vehicleInfo },
  { header: "Mecânico", cell: (o) => o.mechanicName ?? "—" },
  { header: "Status", cell: (o) => <StatusBadge status={o.status} /> },
  { header: "Valor", cell: (o) => <span className="font-mono">{formatCurrency(o.totalAmount)}</span> },
]

export function ServiceOrdersPage() {
  const isMechanic = useHasRole("MECHANIC")
  const canCreate = useHasRole("ADMIN", "RECEPTIONIST")
  // Alterar status: ADMIN, MANAGER e MECHANIC (a OS atribuída a ele) — RECEPTIONIST só acompanha.
  const canChangeStatus = useHasRole("ADMIN", "MANAGER", "MECHANIC")

  const { data, isLoading, error } = useServiceOrders(isMechanic)
  const updateStatus = useUpdateServiceOrderStatus()

  const [creating, setCreating] = useState(false)
  const [toCancel, setToCancel] = useState<ServiceOrder | null>(null)
  const [billing, setBilling] = useState<ServiceOrder | null>(null)

  const actions: RowAction<ServiceOrder>[] = [
    { label: "Faturamento", onSelect: setBilling },
    {
      label: "Avançar status",
      onSelect: (order) => {
        const next = nextStatus(order.status)
        if (next) updateStatus.mutate({ id: order.id, status: next })
      },
      hidden: (order) => !canChangeStatus || !nextStatus(order.status) || isTerminal(order.status),
    },
    {
      label: "Cancelar OS",
      onSelect: setToCancel,
      destructive: true,
      hidden: (o) => !canChangeStatus || isTerminal(o.status),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Ordens de Serviço"
        description="Acompanhe o ciclo de atendimento da oficina"
        action={
          canCreate && (
            <Button onClick={() => setCreating(true)}>
              <Plus /> Nova Ordem
            </Button>
          )
        }
      />
      <DataTable
        data={data}
        columns={columns}
        isLoading={isLoading}
        errorMessage={error ? "Erro ao carregar ordens de serviço" : null}
        emptyMessage={isMechanic ? "Nenhuma ordem de serviço atribuída a você" : undefined}
        actions={actions}
      />
      <ServiceOrderForm open={creating} onClose={() => setCreating(false)} />
      <ServiceOrderBillingDialog order={billing} onClose={() => setBilling(null)} />
      <ConfirmDialog
        open={!!toCancel}
        onOpenChange={(open) => !open && setToCancel(null)}
        title="Cancelar ordem de serviço"
        description={`Cancelar a OS ${toCancel?.orderNumber} (atual: ${toCancel ? STATUS_LABELS[toCancel.status] : ""})? Esta ação não pode ser desfeita.`}
        confirmLabel="Cancelar OS"
        onConfirm={() => toCancel && updateStatus.mutate({ id: toCancel.id, status: "CANCELADA" })}
      />
    </div>
  )
}
