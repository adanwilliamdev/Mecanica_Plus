"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"

import { BrandMark } from "@/components/layout/brand-mark"
import { useAuth } from "@/features/auth/auth-provider"
import { NAV_ITEMS, isActivePath } from "@/lib/navigation"
import { cn } from "@/lib/utils"

/** Conteúdo do menu lateral; usado fixo no desktop e dentro do Sheet no mobile. */
export function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const { user } = useAuth()
  const pathname = usePathname()
  // Só mostra o que a API deixaria o papel logado abrir (evita cair num 403 ao clicar).
  const items = NAV_ITEMS.filter((item) => !user || item.roles.includes(user.role))

  return (
    <>
      <div className="px-6 pt-7 pb-6">
        <div className="flex items-center gap-2.5">
          <BrandMark className="size-[22px] text-amber-400" />
          <span className="font-display text-lg font-semibold tracking-tight text-white">Mecânica+</span>
        </div>
        <p className="mt-1 pl-[30px] text-xs text-graphite-400">Sistema de gestão</p>
      </div>

      <nav aria-label="Principal" className="flex-1 space-y-0.5 px-3">
        {items.map((item) => {
          const active = isActivePath(item.path, pathname)
          return (
            <Link
              key={item.path}
              href={item.path}
              onClick={onNavigate}
              aria-current={active ? "page" : undefined}
              className={cn(
                "group relative flex items-center gap-3 rounded-lg px-3.5 py-2.5 text-sm transition-colors duration-150 outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
                active
                  ? "bg-graphite-800 text-white"
                  : "text-graphite-400 hover:bg-graphite-800/60 hover:text-graphite-100"
              )}
            >
              <span
                aria-hidden
                className={cn(
                  "absolute top-1/2 left-0 h-4 w-[3px] -translate-y-1/2 rounded-r-full bg-amber-400 transition-opacity duration-150",
                  active ? "opacity-100" : "opacity-0"
                )}
              />
              <item.icon
                className={cn("size-5 shrink-0", active ? "text-amber-400" : "text-graphite-500 group-hover:text-graphite-300")}
              />
              <span className="font-medium">{item.label}</span>
            </Link>
          )
        })}
      </nav>

      <div className="border-t border-graphite-800 px-6 py-5">
        <p className="text-xs text-graphite-500">v2.0 · Painel interno</p>
      </div>
    </>
  )
}

export function Sidebar() {
  return (
    <aside className="fixed inset-y-0 left-0 hidden w-64 flex-col bg-graphite-900 text-graphite-200 lg:flex">
      <SidebarContent />
    </aside>
  )
}
