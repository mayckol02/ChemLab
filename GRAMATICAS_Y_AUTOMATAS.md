# Gramáticas y autómatas: implementación interna

Este documento técnico describe los mecanismos internos de validación. La interfaz de juego los presenta como procedimientos químicos; el jugador no necesita conocer gramáticas, producciones, derivaciones ni autómatas para realizar experimentos.

## Protocolos de acciones

`com.chemlab.grammar` modela protocolos como gramáticas regulares deterministas. Una producción `A → acción B` se compila como una transición `A —acción→ B`; una producción `A → ε` identifica el final del procedimiento. `CompiladorGramaticaRegular` convierte la definición a los mismos estados y transiciones que consumen las acciones de `SesionJuego`.

Las definiciones fuente actuales son síntesis de agua (`R_H2O`) y crecimiento de diamante CVD (`R_DIAMANTE_CVD`) en `GramaticasProtocolos`. Las demás reacciones conservan los autómatas definidos por `FabricaAutomata`. En ambos casos, el autómata de la sesión controla el orden permitido de las acciones. El motor químico sigue verificando reactivos, condiciones y productos; el protocolo no reemplaza la química ni la estequiometría.

## Análisis de fórmulas

`AnalizadorFormulaQuimica` usa un parser descendente recursivo para reconocer elementos, subíndices y grupos anidados, por ejemplo `Ca₃(PO₄)₂`. Normaliza subíndices Unicode, verifica símbolos de elementos y calcula el conteo de átomos. En el cuaderno, `VistaCuadernoQuimico` reutiliza el árbol del análisis, añade los nombres y cantidades de los elementos, y presenta la formación del nombre mediante `NomenclaturaQuimica`.

`TablaPeriodica` es el registro inmutable compartido por el parser y la nomenclatura. Cada elemento se define una sola vez con símbolo, nombre, clasificación y, cuando está configurado, sus números de oxidación. Para agregar un elemento reconocido o actualizar esos datos se modifica únicamente esa tabla.

La nomenclatura aplica reglas explícitas y acotadas: Stock para óxidos binarios de elementos con estados de oxidación configurados, prefijos para ciertos óxidos moleculares de no metales y nombres exactos del catálogo cuando no corresponde una regla más específica. Para fórmulas fuera de esas reglas y sin coincidencia de catálogo, la pantalla informa que no hay nomenclatura implementada; no genera nombres especulativos.

El parser limita la entrada a 128 caracteres, 16 niveles de anidamiento y un millón de átomos por elemento. No admite coeficientes externos, cargas, hidratos, puntos de adición ni anotaciones de fase como `(ac)`.

## Correspondencia con la interfaz

- `VistaCuadernoQuimico` presenta los protocolos de todas las reacciones del catálogo recorriendo las transiciones alcanzables de `FabricaAutomata`; para las dos reacciones basadas en gramáticas, esas transiciones se compilan desde `GramaticasProtocolos`. El progreso se marca usando el estado activo o las transiciones del último intento.
- El mismo cuaderno analiza fórmulas de materiales descubiertos, reactivos del experimento seleccionado y productos almacenados; presenta el árbol de fórmula, los nombres de los elementos y la explicación de nomenclatura aplicable.
- La comparación agrupa todos los productos de las reacciones del catálogo y asocia sus rutas de síntesis, incluyendo compuestos con una única ruta. Usa las fichas químicas de `ProtocoloProcedimiento` cuando existen; para otras rutas genera una ficha desde la reacción registrada. El autómata general se construye con las transiciones alcanzables de `FabricaAutomata` e indica si cada ruta es ejecutable en el juego.
- El detalle del historial ofrece una pestaña **Ruta** con el diagrama del autómata del experimento y una pestaña **Etapas** con el recorrido químico traducido.

## Flujo de ejecución

1. El jugador inicia una reacción desde el almacén y se selecciona su autómata.
2. `VistaPuestoLaboratorio` presenta la estación y la acción disponible según el estado actual.
3. `SesionJuego` envía la acción al ejecutor y al autómata; una acción no habilitada no avanza el proceso.
4. La acción aceptada se registra y, cuando corresponde, el motor químico evalúa reactivos, condiciones y productos.
5. El cuaderno muestra la etapa química resultante y conserva el resultado en el historial.

Las definiciones formales de los protocolos son fuente de las transiciones de las reacciones que las usan; la interfaz no crea una secuencia paralela ni permite saltarse la validación del juego.
