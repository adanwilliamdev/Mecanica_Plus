import type { Metadata } from "next"
import "@fontsource-variable/inter"
import "@fontsource-variable/space-grotesk"
import "@fontsource-variable/jetbrains-mono"
import "./globals.css"
import Providers from "./providers"

export const metadata: Metadata = {
  title: "Mecânica+",
  description: "Sistema de gestão para oficinas mecânicas",
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  )
}
