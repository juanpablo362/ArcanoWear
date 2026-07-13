# Arcano Service Wear

Proyecto escolar: app Wear OS + smartphone para el flujo operativo de Arcano Pizza en salón.  
Por ahora solo hay **diseño y lógica simulada** (sin API real).

## Módulos

| Módulo | Dispositivo | Descripción |
|--------|-------------|-------------|
| `:app` | Smartwatch (Wear OS) | Pendientes → detalle/recolección → entrega |
| `:movil` | Teléfono | Login, shell con tabs e inicio por rol |

Ambos usan el mismo `applicationId`: `com.example.arcanowear` (como en `miHolaWatch`, para asociar phone/watch más adelante).

## Cómo correr

1. Abre la carpeta `ArcanoWear` en Android Studio.
2. **Watch:** run configuration `:app` → emulador Wear OS.
3. **Phone:** run configuration `:movil` → emulador o dispositivo Android.

## Credenciales simuladas (móvil)

| Correo | Contraseña | Rol |
|--------|------------|-----|
| `admin@arcano.com` | `admin` | Administrador |
| `despacho@arcano.com` | `despacho` | Despachador |
| `operador@arcano.com` | `operador` | Operador |

## Flujo simulado del watch

1. Lista de pendientes (`Listo` / `Recogida`).
2. Si está **Listo** → detalle → **Confirmar recolección** → `Recogida`.
3. Si está **Recogida** → **Confirmar entrega** → sale de la lista.

## Notas

- Los tabs Órdenes / Historial del teléfono aún son placeholders.
- La comunicación phone ↔ watch (Message API) se agregará después.
