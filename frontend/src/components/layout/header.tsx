"use client"

import { useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { LogOut, Menu } from "lucide-react"

import { SidebarContent } from "@/components/layout/sidebar"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetDescription, SheetTitle } from "@/components/ui/sheet"
import { useAuth } from "@/features/auth/auth-provider"
import { findNavItem } from "@/lib/navigation"

const ROLE_LABELS = {
  ADMIN: "Administrador",
  MANAGER: "Gerente",
  RECEPTIONIST: "Recepção",
  MECHANIC: "Mecânico",
} as const

export function Header() {
  const { user, logout } = useAuth()
  const router = useRouter()
  const pathname = usePathname()
  const [menuOpen, setMenuOpen] = useState(false)

  const initials = (user?.name ?? "")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("")

  const handleLogout = async () => {
    await logout()
    router.push("/login")
  }

  return (
    <header className="sticky top-0 z-30 border-b border-graphite-100 bg-paper/85 backdrop-blur-sm">
      <div className="flex items-center justify-between gap-4 px-4 py-3.5 sm:px-8">
        <div className="flex items-center gap-2">
          <Sheet open={menuOpen} onOpenChange={setMenuOpen}>
            <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setMenuOpen(true)}>
              <Menu />
              <span className="sr-only">Abrir menu</span>
            </Button>
            <SheetContent>
              <SheetTitle>Menu</SheetTitle>
              <SheetDescription>Navegação principal do Mecânica+</SheetDescription>
              <SidebarContent onNavigate={() => setMenuOpen(false)} />
            </SheetContent>
          </Sheet>
          <p className="text-sm font-medium text-graphite-500">{findNavItem(pathname)?.label ?? "Mecânica+"}</p>
        </div>

        <div className="flex items-center gap-3 sm:gap-4">
          <div className="flex items-center gap-3">
            <div className="flex size-8 items-center justify-center rounded-full bg-graphite-800 font-display text-xs font-semibold text-white">
              {initials || "?"}
            </div>
            <div className="hidden text-sm leading-tight sm:block">
              <p className="font-medium text-graphite-800">{user?.name}</p>
              <p className="text-xs text-graphite-400">{user ? ROLE_LABELS[user.role] : ""}</p>
            </div>
          </div>
          <Button variant="ghost" size="icon" onClick={handleLogout} title="Sair">
            <LogOut />
            <span className="sr-only">Sair</span>
          </Button>
        </div>
      </div>
    </header>
  )
}
