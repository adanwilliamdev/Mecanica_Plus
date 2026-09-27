"use client"

import { useState } from "react"
import { zodResolver } from "@hookform/resolvers/zod"
import { Trash2 } from "lucide-react"
import { useForm } from "react-hook-form"
import { z } from "zod"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { useParts } from "@/features/inventory/queries"
import {
  useAddServiceOrderItem,
  useRemoveServiceOrderItem,
  useUpdateLaborAmount,
} from "@/features/service-orders/queries"
import { isTerminal } from "@/features/service-orders/status"
import { formatCurrency } from "@/lib/format"
import type { ServiceOrder } from "@/types"

const itemSchema = z.object({
  partId: z.string().min(1, "Selecione uma peça"),
  quantity: z.number({ error: "Informe a quantidade" }).int("Use um número inteiro").positive("Quantidade deve ser positiva"),
})
type ItemValues = z.infer<typeof itemSchema>

function AddItemForm({ order }: { order: ServiceOrder }) {
  const { data: parts } = useParts()
  const addItem = useAddServiceOrderItem()
  const form = useForm<ItemValues>({ resolver: zodResolver(itemSchema), defaultValues: { partId: "", quantity: 1 } })

  const available = (parts ?? []).filter((p) => p.isActive)

  return (
    <Form {...form}>
      <form
        className="flex flex-wrap items-end gap-3"
        noValidate
        onSubmit={form.handleSubmit((values) =>
          addItem.mutate(
            { orderId: order.id, data: values },
            { onSuccess: () => form.reset({ partId: "", quantity: 1 }) }
          )
        )}
      >
        <FormField
          control={form.control}
          name="partId"
          render={({ field }) => (
            <FormItem className="min-w-56 flex-1">
              <FormLabel>Peça</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl>
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Selecione uma peça" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  {available.map((p) => (
                    <SelectItem key={p.id} value={p.id}>
                      {p.code} · {p.name} ({p.stockQuantity} em estoque · {formatCurrency(p.salePrice)})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="quantity"
          render={({ field }) => (
            <FormItem className="w-28">
              <FormLabel>Qtd.</FormLabel>
              <FormControl>
                <Input
                  type="number"
                  min={1}
                  name={field.name}
                  ref={field.ref}
                  onBlur={field.onBlur}
                  value={field.value ?? ""}
                  onChange={(e) => field.onChange(e.target.value === "" ? undefined : e.target.valueAsNumber)}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <Button type="submit" disabled={addItem.isPending}>
          Adicionar
        </Button>
      </form>
    </Form>
  )
}

function LaborAmountField({ order }: { order: ServiceOrder }) {
  const updateLabor = useUpdateLaborAmount()
  const [value, setValue] = useState(String(order.laborAmount))

  return (
    <div className="flex items-end gap-3">
      <div className="flex-1">
        <label className="text-sm font-medium text-graphite-700">Mão de obra</label>
        <Input
          type="number"
          min={0}
          step="0.01"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          className="mt-1.5"
        />
      </div>
      <Button
        variant="outline"
        disabled={updateLabor.isPending || Number(value) === order.laborAmount}
        onClick={() => updateLabor.mutate({ orderId: order.id, laborAmount: Number(value) || 0 })}
      >
        Salvar
      </Button>
    </div>
  )
}

function ItemsTable({ order, editable }: { order: ServiceOrder; editable: boolean }) {
  const removeItem = useRemoveServiceOrderItem()

  if (order.items.length === 0) {
    return <p className="py-4 text-center text-sm text-graphite-500">Nenhuma peça lançada nesta OS.</p>
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Peça</TableHead>
          <TableHead>Qtd.</TableHead>
          <TableHead>Preço unit.</TableHead>
          <TableHead>Subtotal</TableHead>
          {editable && <TableHead className="w-10" />}
        </TableRow>
      </TableHeader>
      <TableBody>
        {order.items.map((item) => (
          <TableRow key={item.id}>
            <TableCell>
              {item.partCode} · {item.partName}
            </TableCell>
            <TableCell>{item.quantity}</TableCell>
            <TableCell className="font-mono">{formatCurrency(item.unitPrice)}</TableCell>
            <TableCell className="font-mono">{formatCurrency(item.subtotal)}</TableCell>
            {editable && (
              <TableCell>
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Remover peça"
                  disabled={removeItem.isPending}
                  onClick={() => removeItem.mutate({ orderId: order.id, itemId: item.id })}
                >
                  <Trash2 className="text-rust-500" />
                </Button>
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

export function ServiceOrderBillingDialog({ order, onClose }: { order: ServiceOrder | null; onClose: () => void }) {
  const editable = !!order && !isTerminal(order.status)

  return (
    <Dialog open={!!order} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="max-w-lg sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>Faturamento da OS {order?.orderNumber}</DialogTitle>
          <DialogDescription>
            {order?.clientName} · {order?.vehicleInfo}
          </DialogDescription>
        </DialogHeader>

        {order && (
          <div className="space-y-6">
            <section className="space-y-3">
              <h3 className="text-sm font-semibold text-graphite-800">Peças utilizadas</h3>
              <ItemsTable order={order} editable={editable} />
              {editable && <AddItemForm order={order} />}
            </section>

            <section className="space-y-3 border-t border-graphite-100 pt-4">
              {editable ? (
                <LaborAmountField order={order} />
              ) : (
                <p className="text-sm text-graphite-500">
                  Mão de obra: <span className="font-mono">{formatCurrency(order.laborAmount)}</span>
                </p>
              )}
            </section>

            <section className="space-y-1 rounded-xl2 bg-paper-soft/60 p-4 text-sm">
              <div className="flex justify-between text-graphite-600">
                <span>Peças</span>
                <span className="font-mono">{formatCurrency(order.partsAmount)}</span>
              </div>
              <div className="flex justify-between text-graphite-600">
                <span>Mão de obra</span>
                <span className="font-mono">{formatCurrency(order.laborAmount)}</span>
              </div>
              <div className="flex justify-between border-t border-graphite-200 pt-1 text-base font-semibold text-graphite-900">
                <span>Total</span>
                <span className="font-mono">{formatCurrency(order.totalAmount)}</span>
              </div>
            </section>

            {!editable && (
              <p className="text-xs text-graphite-400">
                Esta OS está {order.status === "FINALIZADA" ? "finalizada" : "cancelada"}: o faturamento está
                congelado e não pode mais ser alterado.
              </p>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
