"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { toast } from "sonner"
import { z } from "zod"

import { BrandMark } from "@/components/layout/brand-mark"
import { TextField } from "@/components/form-fields"
import { Button } from "@/components/ui/button"
import { Form } from "@/components/ui/form"
import { useAuth } from "@/features/auth/auth-provider"
import { ApiError } from "@/lib/api-client"
import { getDefaultRouteForRole } from "@/lib/navigation"

const schema = z.object({
  email: z.email("Email inválido"),
  password: z.string().min(6, "Senha deve ter no mínimo 6 caracteres"),
})

type Values = z.infer<typeof schema>

export function LoginForm() {
  const { login } = useAuth()
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const form = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { email: "", password: "" } })

  const onSubmit = async (values: Values) => {
    setIsLoading(true)
    try {
      const user = await login(values)
      toast.success("Login realizado com sucesso!")
      router.push(getDefaultRouteForRole(user.role))
    } catch (error) {
      if (error instanceof ApiError && error.status === 429) {
        toast.error("Muitas tentativas. Aguarde um instante e tente novamente.")
      } else if (error instanceof ApiError && error.status === 0) {
        toast.error(error.message)
      } else {
        toast.error("Email ou senha inválidos")
      }
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-background">
      {/* Painel da marca */}
      <div className="relative hidden flex-col justify-between overflow-hidden bg-graphite-900 text-white lg:flex lg:w-[42%]">
        <div
          aria-hidden
          className="absolute inset-0 opacity-[0.06]"
          style={{
            backgroundImage: "radial-gradient(circle at 1px 1px, white 1px, transparent 0)",
            backgroundSize: "28px 28px",
          }}
        />
        <div className="relative px-12 pt-14">
          <div className="flex items-center gap-2.5">
            <BrandMark className="text-amber-400" />
            <span className="font-display text-lg font-semibold tracking-tight">Mecânica+</span>
          </div>
        </div>
        <div className="relative px-12 pb-16">
          <p className="max-w-sm font-display text-3xl leading-snug">
            Cada ordem de serviço, cada peça, cada cliente — em um só lugar.
          </p>
          <p className="mt-4 max-w-sm text-sm text-graphite-400">
            Sistema de gestão para oficinas que levam a operação a sério.
          </p>
        </div>
      </div>

      {/* Formulário */}
      <div className="flex flex-1 items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="mb-10 flex items-center gap-2.5 lg:hidden">
            <BrandMark className="text-amber-500" />
            <span className="font-display text-lg font-semibold text-graphite-900">Mecânica+</span>
          </div>

          <h2 className="font-display text-2xl font-semibold text-graphite-900">Entrar</h2>
          <p className="mt-1.5 text-sm text-graphite-500">Acesse o painel de gestão da sua oficina.</p>

          <Form {...form}>
            <form className="mt-8 space-y-4" onSubmit={form.handleSubmit(onSubmit)} noValidate>
              <TextField control={form.control} name="email" label="Email" type="email" autoComplete="email" placeholder="voce@oficina.com" />
              <TextField control={form.control} name="password" label="Senha" type="password" autoComplete="current-password" placeholder="••••••••" />
              <Button type="submit" disabled={isLoading} className="mt-2 w-full">
                {isLoading ? "Entrando..." : "Entrar"}
              </Button>
            </form>
          </Form>
        </div>
      </div>
    </div>
  )
}
