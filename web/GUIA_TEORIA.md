# Teoría de cero a experto — Mercado UMSS

> Cómo usar este documento: primero la teoría mínima (niveles 0–5),
> después cada pantalla explicada dato por dato con su **fórmula** y su
> **código** (archivo + método). Si ves algo en pantalla y quieres saber
> de dónde sale, busca aquí su nombre.

---

## Nivel 0 — Las 3 ideas sin las que nada tiene sentido

**1. Población VS muestra.**
La *población* son TODOS los titulados de Sistemas UMSS (cientos, imposible
encuestarlos a todos). La *muestra* son los 61 que sí respondieron.
Todo el programa estima cosas de la población mirando solo la muestra.
Por eso ningún resultado es un número seco: siempre lleva su margen
de duda (el IC 95%, nivel 3).

**2. Variable.**
Una *variable* es una columna: `edad`, `area_interes_posgrado`,
`satisfaccion_formacion`. Cada fila (encuestado) aporta un valor.
Hay dos familias:
- *Numéricas* (edad, años): se promedian.
- *Categóricas* (género, área): se cuentan, no se promedian
  (¿qué sería "promediar" IA con Robótica? No tiene sentido).

**3. Dónde viven los datos.**
CSV semilla → base SQLite (`mercado.db`, tablas `titulado`,
`empleador`, `simulacion`) → servicios → pantallas. Los cálculos
nunca tocan los CSV directamente: leen filas de la base vía
`Titulado.comoMapa()` / `Empleador.comoMapa()`
(`servidor/.../datos/Titulado.java`, `Empleador.java`).

---

## Nivel 1 — Los 6 estadísticos básicos

### n — cuántos respondieron
El denominador de todo. Si `n=7`, cualquier promedio es anecdótico;
si `n=61`, ya se puede concluir. **Código:** `ResumenNumerico.totalDatos`
(`nucleo/.../stats/ResumenNumerico.java`, lo llena `Numeros.resumir`).

### Media — el promedio
```
x̄ = (x₁ + x₂ + ... + xₙ) / n
```
Ejemplo: edades 23, 30, 45 → (23+30+45)/3 = 32.67.
Es sensible a extremos: un jubilado de 70 entre jóvenes la jala.
**Código:** `Numeros.resumir(...).media()` usa `DescriptiveStatistics.getMean()`
(`nucleo/.../stats/Numeros.java`).

### Mediana — el del medio
Se ordenan los valores y se toma el central (con n par, el promedio de
los dos centrales). No la mueven los extremos.
Regla: **media ≈ mediana → distribución simétrica**; si difieren mucho,
hay un grupo extremo jalando la media (pasa en
`concordancia_formacion`: media 2.61 VS mediana 3.00).
**Código:** percentil 50 en `Numeros.resumir`.

### Desviación estándar (desv) — qué tan repartidas están las opiniones
```
s = √( Σ(xᵢ − x̄)² / (n − 1) )
```
Se lee así: es la "distancia típica" a la media. `desv=0` = todos
respondieron idéntico. Se divide entre `n−1` (no entre `n`) porque con
muestra pequeña eso corrige el sesgo: es la *desviación muestral*.
Regla de bolsillo en escala 1–4: `<0.5` todos piensan igual,
`0.5–1.0` dispersión moderada, `>1.0` opiniones divididas.
**Código:** `getStandardDeviation()` (ya muestral) en `Numeros.resumir`.

### Mín / máx / cuartiles — el rango
Q1 deja 25% debajo, Q2 = mediana, Q3 deja 75% debajo.
Sirven para ver asimetrías de un vistazo.
**Código:** percentiles 25/50/75 en `Numeros.resumir`.

### Frecuencia y moda — para categorías
*Frecuencia* = conteo ("IA: 14"). *Relativa* = conteo/n ("23%").
*Moda* = la categoría ganadora. Los vacíos se agrupan como `(VACIO)`
para que la no-respuesta se vea en vez de ocultarse.
**Código:** `Frecuencias.contar/moda` (`nucleo/.../stats/Frecuencias.java`).

---

## Nivel 2 — Escala Likert (texto → número)

Medir "satisfacción" con texto no permite promediar, así que cada
etiqueta se traduce a puntos. Esta es TODA la traducción
(`nucleo/.../likert/EscalaLikert.java`, método `traducir`):

```
1 = TOTALMENTE EN DESACUERDO / INSATISFECHO / NO IMPORTANTE
2 = EN DESACUERDO / ALGO SATISFECHO / POCO IMPORTANTE
3 = DE ACUERDO / SATISFECHO / IMPORTANTE
4 = TOTALMENTE DE ACUERDO / MUY IMPORTANTE
```

**Por qué unas son 1–4 y otra 1–3:** depende de las opciones que traía
la pregunta en la encuesta real. `satisfaccion_negocio` solo ofrecía
*Insatisfecho / Algo satisfecho / Satisfecho* → techo 3
(ver `escalaMaximaDe`). La de empleadores usa 1, 3 y 4
(el 2 no existe ahí) y su `NO_SABE` se excluye (llega como NaN y se
salta en `ServicioAnalisis.likertDe`).

Lectura de una media Likert (`etiquetaDeMedia`):
`1–1.5` rechazo · `1.5–2.5` desacuerdo · `2.5–3.0` tibio ·
`3.0–3.5` de acuerdo · `3.5–4` acuerdo total.

---

## Nivel 3 — IC 95% (la "media real") y precisión

**La idea en una frase:** tu media (ej. 7.43) se calculó con 61
personas, no con toda la población. La *media real* (la de TODOS los
titulados) es desconocida, pero con 95% de confianza cae dentro del
intervalo. Ancho chico = estimación precisa (n grande o poca
dispersión); ancho grande = imprecisa.

**Fórmulas** (`nucleo/.../stats/IntervalosConfianza.java`):
```
Medias:        x̄ ± t(0.975, n−1) · s / √n      (t de Student, ideal con n chico)
Proporciones:  p̂ ± 1.96 · √( p̂(1−p̂) / n )      (Z normal; se recorta a [0,1])
Diferencias:   (m1−m2) ± t · sp·√(1/n1 + 1/n2)  (sp = desv. agrupada)
```
La `t` la calcula `TDistribution` de Commons Math (adiós tablas).
Ejemplo: antigüedad `7.43 ± 4.97, IC [6.18, 8.67]` se lee
"la antigüedad real media está entre 6.2 y 8.7 años".
Si el IC de una *diferencia* incluye al 0, los grupos NO difieren
(`paraDiferencia`, usado en segmentación junior VS consolidado).

---

## Nivel 4 — Chi-cuadrada (¿están relacionadas dos preguntas?)

**La idea:** se cruzan dos categóricas en una matriz de conteos
*observados* O. Si fueran independientes, cada celda tendría
`E = (total_fila × total_columna) / n` (*esperados*). La distancia
total es:
```
χ² = Σ (O − E)² / E        gl = (filas−1) × (columnas−1)
```
`p` = probabilidad de ver un χ² así de grande por puro azar.
**Regla de oro: `p < 0.05` → SÍ hay relación; `p ≥ 0.05` → no hay
evidencia.** Todo lo calcula `ChiSquareTest` de Commons Math
(`nucleo/.../stats/PruebaChiCuadrada.java`, método `cruzar`).
En pantalla el heatmap pinta cada celda según `conteo/máximo`
(`vista/VistaCruces.java`, método `mapaDeCalor`): más oscuro = más casos.

---

## Nivel 5 — Monte Carlo ligero (pronóstico sin inventar datos)

El algoritmo exacto (`servidor/.../servicio/ServicioSimulacion.java`,
método `simularTodo`), 1000 repeticiones por escenario:
```
1. p = afines_al_área / total_titulados        (dato REAL, no inventado)
2. Repetir 1000 veces:
     inscritos = 0
     Por cada titulado:
        prob = 0.8 si tiene interés SI en el área, si no p·0.2
        si Random() < prob → inscritos++
     guardar inscritos
3. Demanda esperada = media de los 1000; rango = su IC95 (t de Student)
4. Guardar la corrida en la tabla `simulacion` (historial auditable)
```
Leer "espera unos 13.4 (entre 13.3 y 13.5)" = media e IC del paso 3.

---

## Pantalla 1 — Panel `/`

| Lo que ves | Qué es | Fórmula / entrada | Código |
|---|---|---|---|
| Antigüedad media `X ± Y` | Media y desv de `anios_vida_profesional` | Nivel 1 | `ServicioAnalisis.resumenNumerico` → `Numeros.resumir` |
| Tasa de inserción + "(entre A y B de cada 100)" | % con `situacion ≠ NO_TRABAJA` (vacías fuera) + su IC | `favorables/con_respuesta`; Nivel 3 | `tasaInsercion()` + `rangoDe()` |
| Quieren posgrado | % con `interes == SI` + IC | Igual que arriba | `intencionPosgrado()` |
| Necesitarían beca | % con `financiamiento` que contiene BECA | Igual que arriba | `proporcionRequiereBeca()` |
| Recomendaciones | Reglas si-entonces sobre los números | Umbrales 30% / 50% / 3.0 / 25% | `ServicioRecomendaciones.generar()` |

## Pantalla 2 — Titulados `/titulados`

**¿Por qué 8 columnas si el CSV tiene 36?** Decisión de diseño, no
pérdida: las 36 están en la base (y entran completas por CSV en
`/importar`). La tabla muestra las 8 que alimentan los indicadores
(ID, edad, género, situación, años, área, financiamiento,
satisfacción) para que quepa en pantalla. Para agregar una columna:
una línea en `VistaTitulados.java` (ej. `tabla.addColumn(
Titulado::getNivelPosgrado).setHeader("Nivel");`).

| Lo que ves | Qué es | Código |
|---|---|---|
| Filtro situación | Recorta SOLO la tabla (los resúmenes usan toda la muestra) | `refrescarTabla()` |
| Antigüedad: "Media X ± Y… entre A y B" | Nivel 1 + Nivel 3 (t de Student) | `resumenNumerico("anios_vida_profesional")` + `IntervalosConfianza.paraMedia` |
| Satisfacción: "Media 3.05: de acuerdo…" | Likert traducido + etiqueta | `likertDe` + `EscalaLikert.etiquetaDeMedia/DeConsenso` |
| Top áreas | Top 5 frecuencias sin `(VACIO)` | `topCategorias(frecuenciasDe(...), 5)` |

## Pantalla 3 — Empleadores `/empleadores`

| Lo que ves | Qué es | Código |
|---|---|---|
| Tabla empresas | 6 columnas clave de las 24 guardadas | `VistaEmpleadores.java` |
| Barras de acuerdo | % con puntaje ≥ 3 por ítem (lista `ITEMS_LIKERT`) | `likertDe` + `barraDeAcuerdo()` (ProgressBar) |
| Competencias top | Palabras más repetidas del texto libre (mín. 3 letras, sin vacías) | `ServicioAnalisis.competenciasMencionadas(10)` |
| Cobertura | N empresas distintas + ¿hay pública? + ¿hay privada? (mínimo 3/1/1, §5 guía) | Bloque final de `VistaEmpleadores.java` |

## Pantalla 4 — Cruces `/cruces`

Cada sección = `FichaCruce(etiqueta, colA, colB, resultado, frase)`:
matriz observada (`resultado.observados()`), línea
`χ²=… gl=… p=…`, y frase (`GuiasLectura.fraseDependencia`).
Los 5 cruces de titulados + 2 de empleadores salen de
`ServicioCruces.crucesTitulados()/crucesEmpleadores()`; la columna
`segmento_antiguedad` no existe en la BD: se deriva de los años en
`columnaDerivada()` (Junior 0–3 / Intermedio 4–7 / Consolidado 8+,
cortes de la guía en `ServicioAnalisis.segmentoDe`).

## Pantalla 5 — Simulación `/simulacion`

Botón → `ServicioSimulacion.simularTodo()` (Nivel 5) → una frase por
escenario + tabla historial (`Simulacion`, ordenada por fecha vía
`RepositorioSimulaciones.findAllByOrderByFechaHoraDesc()`).
Cada clic guarda 5 filas nuevas: el pronóstico queda registrado.

## Pantalla 6 — Registro `/registro`

**¿Por qué no están las 36 preguntas?** A propósito, por dos motivos:
1. El formulario trae los ~14 campos que **alimentan indicadores de
   las HU** (HU1: edad, situación, años; HU2: interés, área;
   HU3: financiamiento; satisfacción/consordancia; contacto).
   Campos puramente descriptivos (las 9 listas de materias
   ventajosas/inútiles, aspectos con `|`, emprendimiento detallado)
   **sí están en la base y sí entran por CSV**, pero en el formulario
   solo harían ruido: ningún indicador los usa.
2. Listas cerradas en vez de texto libre para que las categorías
   salgan normalizadas y los cruces no se fragmenten.

Mapa campo→HU: `VistaRegistro.formularioTitulado/formularioEmpleador`.
Registro completo (36/24 columnas): pantalla `/importar` → `Titulado.desdeMapa` /
`Empleador.desdeMapa` (espejos exactos del CSV).

## Pantalla 7 — Importar `/importar` + la base

Sube CSV → `LectorCsv.leer` → `LimpiezaDatos.limpiarFila` →
`desdeMapa` → `save`, saltando id ya guardados
(`ServicioCarga.cargarTitulados/cargarEmpleadores`). Al arrancar con
la base vacía, `CargadorInicial` importa los semilla. Conexión y
tablas automáticas: `application.properties` + `@Entity`.

---

## Receta novato: agregar un campo a todo el circuito
1. `datos/Titulado.java`: campo + getter/setter + 1 línea en `comoMapa` y 1 `case` en `desdeMapa`.
2. Arranca: Hibernate agrega la columna sola (`ddl-auto: update`).
3. Úsalo: `analisis.columnaDe(mapas, "tu_columna")` en un servicio y muéstralo en una vista.
Sin tocar SQL jamás.
