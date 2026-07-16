# Arcano Service Wear

Proyecto escolar: app Wear OS + smartphone para el flujo operativo de Arcano Pizza en salón.  
Por ahora solo hay **diseño y lógica simulada** (sin API real).

## Módulos

| Módulo | Dispositivo | Descripción |
|--------|-------------|-------------|
| `:app` | Smartwatch (Wear OS) | Pendientes → detalle/recolección → entrega |
| `:movil` | Teléfono | Login y pantallas por rol |

Ambos usan el mismo `applicationId`: `com.example.arcanowear` (como en `miHolaWatch`, para asociar phone/watch más adelante).

## Cómo correr

1. Abre la carpeta `ArcanoWear` en Android Studio.
2. **Watch:** run configuration `:app` → emulador Wear OS.
3. **Phone:** run configuration `:movil` → emulador o dispositivo Android.

## Login (API real)

El móvil autentica contra:

`POST https://arcanopizzaapi-ezetgtdugefxcybf.eastus-01.azurewebsites.net/api/Auth/login`

Body:

```json
{ "correo": "tu@correo.com", "password": "tuPassword" }
```

Usa usuarios reales de la BD (roles **Administrador**, **Despachador** u **Operador**).  
El token JWT se guarda en memoria (`SessionRepository`).

### Admin usuarios (API real)

- Listar: `GET /api/admin/usuarios`
- Crear: `POST /api/admin/usuarios` (`nombre`, `email`, `tipo`, `password`)
- Editar: `PUT /api/admin/usuarios/{id}`
- Activar/desactivar: `PATCH /api/admin/usuarios/{id}/toggle`

Mesas, órdenes, historial y watch siguen **simulados**.

## Pantallas por rol (móvil)

### Administrador — nav: Usuarios · Mesas · Historial · Cuenta

- Entra en **Usuarios**
- CRUD simulado de usuarios (activar/desactivar, roles)
- Mesas (agregar / ciclar estado)
- Historial con búsqueda

### Operador — nav: Nueva · Cobrar · Historial · Cuenta

- Entra en **Nueva orden**
- **Cobrar** = cerrar mesa / pago
- Historial

### Despachador — nav: Gestión · Historial · Cuenta

- Entra en **Gestión** (filtros + Avanzar hasta `Listo`)
- Historial

## Flujo simulado del watch

1. Lista de pendientes (`Listo` / `Recogida`).
2. Si está **Listo** → detalle → **Confirmar recolección** → `Recogida`.
3. Si está **Recogida** → **Confirmar entrega** → sale de la lista.

## Notas

- Solo **login** y **usuarios (admin)** usan la API; mesas, órdenes, historial y watch siguen mock.
- La comunicación phone ↔ watch (Message API) se agregará después.
