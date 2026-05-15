# 📘 Práctica Guiada: Gestión de Roles y Permisos (RBAC)

## 🎯 Objetivo

Implementar un sistema de control de acceso basado en roles (RBAC) combinando:
- **SecurityConfig** → Define qué URLs son públicas y cuáles requieren autenticación
- **@PreAuthorize** → Define qué roles pueden acceder a cada endpoint específico

## 🏗️ Arquitectura de Permisos

### Niveles de Acceso

| Nivel | Descripción | Ejemplo |
|-------|-------------|---------|
| 🔓 **Público** | Sin login necesario | Ver cartelera, horarios |
| 🔑 **USUARIO** | Login como usuario normal | Comprar entradas, ver sus ventas |
| 🛡️ **ADMINISTRADOR** | Login como admin | CRUD completo de todo |

### Matriz de Permisos por Entidad

| Entidad | GET (Listar) | GET (Detalle) | POST (Crear) | PUT (Editar) | DELETE (Borrar) |
|---------|:---:|:---:|:---:|:---:|:---:|
| **Películas** | 🔓 Público | 🔓 Público | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Funciones** | 🔓 Público | 🔓 Público | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Salas** | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Ventas** | 🛡️ Admin | 🔑 Propietario/Admin | 🔑 Usuario/Admin | 🛡️ Admin | 🔑 Propietario/Admin |
| **Mis Ventas** | 🔑 Usuario | - | - | - | - |
| **Entradas** | 🛡️ Admin | 🔑 Usuario/Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Usuarios** | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Mi Perfil (/me)** | 🔑 Usuario/Admin | - | - | - | - |
| **Actores** | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Directores** | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |
| **Roles** | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin | 🛡️ Admin |

---

## 📝 Paso 1: Configuración en `SecurityConfig.java`

El primer nivel de seguridad se define en la configuración central. Aquí establecemos qué URLs son públicas y cuáles requieren autenticación:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ← Esto activa @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // URLs públicas (según el enunciado)
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/error/**").permitAll()
                
                // Cartelera pública: GET películas y funciones
                .requestMatchers("GET", "/api/v1/peliculas/**").permitAll()
                .requestMatchers("GET", "/api/v1/funciones/**").permitAll()
                
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### ¿Qué hace cada línea?

- `@EnableMethodSecurity` → Activa las anotaciones `@PreAuthorize` en los controladores
- `requestMatchers("/api/v1/auth/**").permitAll()` → Login/Registro sin token
- `requestMatchers("GET", "/api/v1/peliculas/**").permitAll()` → Solo los GET son públicos
- `.anyRequest().authenticated()` → Todo lo demás necesita JWT

---

## 📝 Paso 2: `@PreAuthorize` a nivel de CLASE

Para entidades donde **todos** los endpoints son solo para ADMIN, usamos `@PreAuthorize` a nivel de clase:

```java
@RestController
@RequestMapping("/api/v1/salas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')") // ← TODO el controlador es ADMIN
public class SalaController {
    // Todos los métodos heredan la restricción de la clase
    @GetMapping
    public ResponseEntity<List<SalaOutputDTO>> findAll() { ... }
    
    @PostMapping
    public ResponseEntity<SalaOutputDTO> create(...) { ... }
}
```

**Se aplica igual a:** `ActorController`, `DirectorController`, `RolController`

---

## 📝 Paso 3: `@PreAuthorize` a nivel de MÉTODO

Para entidades con permisos mixtos (unos públicos, otros protegidos), usamos `@PreAuthorize` en cada método:

```java
@RestController
@RequestMapping("/api/v1/peliculas")
public class PeliculaController {

    // 🔓 PÚBLICO — No lleva @PreAuthorize (ya es público en SecurityConfig)
    @GetMapping
    public ResponseEntity<List<PeliculaOutputDTO>> findAll() { ... }

    // 🔒 SOLO ADMIN — Requiere rol ADMINISTRADOR
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PeliculaOutputDTO> create(...) { ... }
}
```

---

## 📝 Paso 4: Lógica de Pertenencia (Ownership)

Para las ventas, un USUARIO solo puede ver/cancelar **sus propias** ventas. La verificación se hace en el **servicio**:

```java
// En VentaController.java
@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
public ResponseEntity<VentaOutputDTO> findById(@PathVariable Long id, Authentication auth) {
    return ResponseEntity.ok(ventaService.findByIdSecured(id, auth));
}

// En VentaService.java
public VentaOutputDTO findByIdSecured(Long id, Authentication auth) {
    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

    // ADMIN puede ver cualquier venta
    if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
        return ventaMapper.toDTO(venta);
    }

    // USUARIO solo puede ver las suyas
    if (venta.getUsuario().getEmail().equals(auth.getName())) {
        return ventaMapper.toDTO(venta);
    }

    throw new RuntimeException("No tienes permiso para ver esta venta.");
}
```

### Endpoint especial: `/mis-ventas`

```java
@GetMapping("/mis-ventas")
@PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
public ResponseEntity<List<VentaOutputDTO>> findMyVentas(Authentication auth) {
    return ResponseEntity.ok(ventaService.findByUserEmail(auth.getName()));
}
```

---

## 📝 Paso 5: Endpoint de Perfil Propio (`/me`)

Cada usuario puede ver su propio perfil sin necesitar el ID:

```java
@GetMapping("/me")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
public ResponseEntity<UsuarioOutputDTO> getMyProfile(Authentication auth) {
    return ResponseEntity.ok(usuarioService.findByEmail(auth.getName()));
}
```

---

## 📝 Paso 6: Cómo Spring traduce los Roles

En `CineUserDetailsService`, los roles se traducen al formato de Spring:

```java
// Nuestro rol en BD:  "ADMINISTRADOR"
// Spring lo traduce a: "ROLE_ADMINISTRADOR"
.map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
```

Por eso en `@PreAuthorize` usamos `hasRole('ADMINISTRADOR')` (Spring añade el prefijo `ROLE_` automáticamente).

---

## 🧪 Cómo Probar

### Test 1: Acceso Público
```
GET /api/v1/peliculas → 200 OK (sin token)
GET /api/v1/funciones → 200 OK (sin token)
POST /api/v1/peliculas → 401 Unauthorized (sin token)
```

### Test 2: Acceso como USUARIO
```
POST /api/v1/auth/login → Obtener token de USUARIO
GET /api/v1/ventas/mis-ventas → 200 OK (ve sus ventas)
GET /api/v1/ventas → 403 Forbidden (solo ADMIN puede ver todas)
POST /api/v1/salas → 403 Forbidden (solo ADMIN)
```

### Test 3: Acceso como ADMIN
```
POST /api/v1/auth/login → Obtener token de ADMIN
GET /api/v1/ventas → 200 OK (ve todas las ventas)
POST /api/v1/salas → 201 Created (puede crear salas)
DELETE /api/v1/peliculas/1 → 204 No Content (puede borrar)
```

---

## 📊 Resumen de Anotaciones Usadas

| Anotación | Significado |
|-----------|-------------|
| `@PreAuthorize("hasRole('ADMINISTRADOR')")` | Solo accesible con rol ADMINISTRADOR |
| `@PreAuthorize("hasRole('USUARIO')")` | Solo accesible con rol USUARIO |
| `@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")` | Accesible con cualquiera de los dos roles |
| Sin anotación + `permitAll()` en SecurityConfig | Acceso público sin token |
