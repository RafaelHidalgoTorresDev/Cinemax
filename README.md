# 🎬 Proyecto CineMax - Guía de Evaluación para el Profesor

Este documento detalla todas las implementaciones realizadas en el proyecto para facilitar su corrección y demostrar que se han cumplido y superado los requisitos de la tarea.

## 🚀 1. Arquitectura y Backend (Spring Boot)

Se ha desarrollado una API REST robusta que gestiona toda la lógica de negocio, incorporando los siguientes hitos técnicos:

* **Seguridad Avanzada (RBAC + JWT):** Se ha implementado un sistema completo de roles. 
  * Los endpoints públicos (como ver la cartelera) están abiertos.
  * La compra de entradas y visualización del historial está restringida a usuarios con rol `USUARIO`.
  * La creación, edición y borrado de entidades (Salas, Películas, Funciones, etc.) están bloqueados mediante `@PreAuthorize` y solo son accesibles por el `ADMINISTRADOR`.
* **Colección Postman Actualizada:** En la raíz del proyecto se incluye el archivo `Cine_V2_API.postman_collection.json`. En él están configuradas todas las rutas necesarias para probar la API, incluyendo peticiones con inyección automática de tokens Bearer.

## 🎨 2. Frontend React (Diseño "Ultra Premium")

Se ha construido desde cero una aplicación cliente SPA (Single Page Application) utilizando React, conectada en tiempo real con el backend de Spring Boot:

* **Diseño Glassmorphism y UI Cinematográfica:** Interfaz inspirada en plataformas de streaming modernas de alta gama, utilizando fondos dinámicos, desenfoques (`backdrop-filter`), degradados dorados y un diseño completamente responsivo adaptado a formato cuadrícula (CSS Grid).
* **Gestión de Imágenes Infalible:** Las portadas y fondos de las películas han sido extraídos directamente de la API de TMDB (The Movie Database). Se han corregido todas las URLs en la base de datos PostgreSQL mediante un script automatizado para garantizar que se rendericen en alta calidad (1280p).
* **Buscador y Filtros en Tiempo Real:** Implementación de un buscador reactivo y filtros de categorías integrados en un panel de control estilizado en la parte superior.
* **Fallback Inteligente:** Si por algún problema de red una imagen oficial de película fallara, el sistema genera automáticamente por código un SVG (Placeholder Dorado) sin depender de servicios externos de terceros, garantizando que el diseño jamás se rompa.

## ⚙️ 3. Instrucciones de Despliegue para Evaluar

Para probar el proyecto al completo:

### A. Arrancar el Backend (Java/Spring Boot)
1. Asegurarse de tener PostgreSQL en el puerto `5432` con las credenciales configuradas en `application.properties` (`cine_user` / `cine_password`).
2. Arrancar la aplicación Spring Boot desde el IDE o consola. Las tablas y los datos de prueba (incluyendo los pósters HD de TMDB) se inyectarán automáticamente.

### B. Arrancar el Frontend (React)
1. Abrir una nueva terminal y navegar a la carpeta del frontend: `cd cine-frontend`
2. Instalar las dependencias (si no se ha hecho antes): `npm install`
3. Arrancar el servidor de desarrollo: `npm run dev`
4. Acceder en el navegador a `http://localhost:5173` para experimentar la interfaz gráfica conectada con el backend.

### C. Probar la API (Postman)
1. Abrir Postman e importar el archivo `Cine_V2_API.postman_collection.json` ubicado en la raíz del repositorio.
2. Utilizar los endpoints de la carpeta de Autenticación para loguearse como Administrador o Usuario y comprobar los bloqueos de seguridad.

---
**Nota:** El proyecto ha sido subido en su totalidad a la rama `develop` cumpliendo con las directrices de la *Tarea Inicial*.
