# Nuevas funcionalidades de ChemLab

## Resumen

Este documento describe las funcionalidades implementadas y las mejoras que siguen pendientes. ChemLab combina un laboratorio 2D, prácticas químicas guiadas por autómatas y gramáticas, y persistencia local por perfiles. No utiliza una base de datos.

| Funcionalidad | Estado |
|---|---|
| 1. Intro que se cierra manualmente | Implementada |
| 2. Retroalimentación contextual durante las prácticas | Implementada |
| 3. Resumen al completar o fallar una práctica | Implementado |
| 4. Misiones opcionales en Modo Difícil | Implementadas |
| 5. Perfiles y guardado de progreso | Implementados |
| 6. Simulación observable y alertas educativas | Implementadas |
| 7. Accesibilidad y alternativas de control | Pendiente |
| 8. Prácticas declarativas en JSON compiladas a autómatas | Implementadas para las 25 reacciones |

## Estado por funcionalidad

### 1. Intro con avance manual

Después de seleccionar un perfil, se muestra la intro animada de ChemLab. La animación puede terminar, pero la pantalla no avanza por tiempo: el usuario la cierra con **Espacio** o **Enter**. La pista de audio es opcional.

La orientación sobre las prácticas se ofrece dentro del Modo Tutorial; no hay un recorrido independiente de primer uso.

### 2. Retroalimentación contextual durante la práctica

Cada acción informa el resultado y la etapa alcanzada. En **Modo Tutorial**, los pasos válidos indican cómo continuar y los errores explican qué revisar. En **Modo Difícil**, las sugerencias invitan a analizar los materiales, las condiciones y el efecto observado.

La explicación permanece visible en la consola del puesto mientras la práctica sigue activa; los avisos temporales complementan esa información, y los de error duran más que los avisos normales.

### 3. Resumen de la práctica

Al completar o fallar una práctica, se muestra un resumen con el objetivo, la ecuación disponible, los productos realmente registrados, una explicación de aprendizaje o aspectos por revisar, y la secuencia de operaciones. Desde allí se puede volver al laboratorio o abrir el cuaderno. El resumen no presenta productos que no se hayan generado.

### 4. Misiones opcionales y progresión

La ruta introductoria consta de cinco prácticas: formación de agua, combustión del carbono, síntesis de cloruro de sodio, disolución de sal y recuperación del soluto por evaporación. El panel **Misiones · G** está disponible únicamente en **Modo Difícil**.

Al iniciar la ruta, se habilita la misión actual; completarla desbloquea la siguiente. Un intento fallido no hace avanzar la ruta. Al completar la última misión, las prácticas vuelven a quedar accesibles. El progreso se conserva en el perfil.

El **Modo Tutorial** se mantiene independiente: permite seleccionar cualquier práctica disponible sin iniciar ni avanzar misiones.

### 5. Perfiles y guardado local

Antes de la intro se puede seleccionar un perfil existente o crear uno. También se puede volver al selector desde el laboratorio para cambiar de perfil. Cada perfil conserva por separado el avance de misiones, las sustancias descubiertas, los procesos completados y el historial/resultados del cuaderno.

Los perfiles se guardan en archivos JSON locales identificados por UUID, dentro de `.chemlab/profiles` en la carpeta personal del usuario. La escritura es atómica; no se usa una base de datos. Se reanuda el progreso entre prácticas, no una práctica que estuviera activa.

Al cambiar de perfil durante una práctica activa, se solicita confirmación y, si se acepta, se registra el intento como abandonado antes de guardar. Se rechazan nombres vacíos, demasiado largos o duplicados sin distinguir mayúsculas de minúsculas. Los errores de lectura, corrupción o escritura se notifican explícitamente.

### 6. Simulación observable y alertas educativas

La consola del puesto presenta temperatura, presión, pH y atmósfera actuales; en Tutorial también muestra las condiciones objetivo del paso. Durante las operaciones guiadas, las lecturas de temperatura y presión acompañan la animación. Se muestran el fenómeno modelado, los reactivos, los productos y sus fases previstas, además de las desviaciones respecto a las condiciones definidas para la reacción.

El monitor genera alertas educativas ante temperaturas o presiones elevadas, atmósferas de oxígeno o cloro y solicitudes de ignición en esas atmósferas. Las alertas no bloquean acciones ni cambian las reglas químicas. Sus umbrales son propios de la simulación y no representan límites seguros reales.

### 7. Accesibilidad y alternativas de control — Pendiente

Ampliar la accesibilidad con alternativas de teclado para interacciones de arrastre, ajustes de tamaño de texto, controles de audio y una opción para reducir movimiento y animaciones. Estas mejoras no están implementadas todavía.

### 8. Prácticas externas y autómatas compilados desde gramáticas

El manifiesto [catalogo-quimico.json](src/main/resources/catalogo-quimico.json) conserva las sustancias y reacciones y enumera las rutas de práctica. Cada una de las 25 reacciones tiene un archivo independiente bajo `src/main/resources/practicas/`. Por ejemplo, [R_MgO.json](src/main/resources/practicas/R_MgO.json) declara materiales, objetivo, equipo, condiciones, etapas educativas, fases observables y gramática de acciones.

El cargador valida las referencias y la estructura de las prácticas, incluida la gramática y su recorrido hasta un estado final. La fábrica de experimentos crea los experimentos desde esas definiciones; el compilador convierte cada gramática regular en un autómata finito, que valida y ejecuta las acciones durante la partida.

Como parte de esta migración, se retiraron las definiciones de protocolos y configuraciones por reacción que estaban duplicadas en Java, incluidas las gramáticas específicas de agua y CVD que estaban en `GramaticasProtocolos`. Las fábricas ya no seleccionan lógica de autómatas o experimentos por ID de reacción. Se conserva el motor común, el compilador de gramáticas y los autómatas genéricos que usan escenarios no asociados a una práctica declarativa.

**Alcance pendiente de migración:** la planificación de puestos físicos y ciertos metadatos de rutas alternativas de síntesis siguen en código Java. Por tanto, los datos de las prácticas y sus gramáticas son declarativos, pero no toda decisión de presentación o navegación entre estaciones se configura desde JSON.

Para agregar una práctica, se registran sus sustancias y reacción en el catálogo, se crea su archivo JSON y se añade la ruta al manifiesto. La validación automática detecta referencias y recorridos inválidos, pero no certifica el balance químico, las condiciones experimentales ni la seguridad; esos aspectos requieren revisión independiente.

## Próximos pasos recomendados

1. Implementar y probar las mejoras de accesibilidad y control.
2. Revisar químicamente las prácticas existentes y cada nueva práctica antes de incorporarla al catálogo.
3. Decidir por separado si la planificación de estaciones y las rutas alternativas también deben convertirse en datos declarativos.
