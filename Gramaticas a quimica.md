# De gramáticas a química en ChemLab

## Objetivo del cambio

ChemLab es un juego de laboratorio químico, no un curso de teoría de lenguajes. El jugador debe comprender qué preparar, qué equipo utilizar y qué producto espera obtener, sin tener que aprender términos como gramática, producción, derivación o autómata.

La implementación conserva esos mecanismos como parte de la lógica interna. Las gramáticas describen el orden válido de algunas secuencias de laboratorio y se compilan a autómatas; el motor químico verifica las sustancias, condiciones y productos. La nueva interfaz traduce ese funcionamiento a tareas y conceptos químicos.

## Experiencia del cuaderno

Al abrir el cuaderno con **E**, están disponibles sus secciones habituales y el **Cuaderno químico**, con tres herramientas:

### Protocolos de síntesis

Presenta los protocolos de todos los procesos incluidos en el catálogo de reacciones. El selector incluye también los procesos que no tienen una gramática fuente independiente: sus etapas se recorren desde las transiciones alcanzables del autómata químico, con sus acciones traducidas y el equipo asociado a cada estado.

Cuando existe información de manejo específica en `ProtocoloProcedimiento`, se utiliza para describir las etapas. En los procesos restantes se muestra información experimental genérica en función del estado, conservando la secuencia real del autómata.

Cada etapa indica su acción en lenguaje de laboratorio y el equipo requerido. Si una reacción está en curso, la vista destaca las transiciones disponibles desde el estado actual. Si existe un intento previo, marca las transiciones válidas que quedaron registradas; los experimentos completados aparecen como tales.

Las etapas proceden de las transiciones de `FabricaAutomata`; para las reacciones definidas mediante gramáticas, esa fábrica compila la gramática correspondiente. La interfaz no mantiene una secuencia paralela. El protocolo guía, pero las acciones reales siguen pasando por `SesionJuego`, el autómata y el motor químico.

### Análisis de muestras

Permite elegir una sustancia descubierta, una que esté en el inventario de un experimento visible o un producto guardado. También se puede escribir manualmente una fórmula química para examinarla.

El resultado muestra la fórmula normalizada, los nombres de los elementos, la cantidad de átomos por elemento y el total por unidad de fórmula. Por ejemplo, `Ca₃(PO₄)₂` se desglosa en calcio: 3, fósforo: 2 y oxígeno: 8.

También presenta un árbol desplegable que conecta tres etapas: la estructura de la fórmula reconocida, los elementos y sus cantidades, y la formación del nombre químico. Para hidróxidos y óxidos binarios comunes aplica nomenclatura Stock y explica progresivamente la clasificación y el balance de cargas. Para óxidos moleculares de no metales aplica prefijos numéricos. Si la fórmula coincide con una sustancia del catálogo, usa su nombre registrado cuando no aplica una regla específica; para familias no implementadas evita inventar un nombre y lo indica explícitamente. El árbol se construye con el resultado de `AnalizadorFormulaQuimica` y las reglas de `NomenclaturaQuimica`; no es una lección formal de gramática.

El análisis admite un coeficiente estequiométrico inicial (por ejemplo, `4Fe(OH)₃`) y etiquetas de fase `(ac)`, `(aq)`, `(s)`, `(l)` y `(g)`. El coeficiente se distingue de los subíndices: multiplica unidades de fórmula y el árbol lo señala explícitamente. La etiqueta de fase se conserva para mostrar el estado físico y priorizar la sustancia correcta del catálogo, por ejemplo una solución acuosa frente a la sal sólida.

El botón **Subíndice ₂** convierte los dígitos seleccionados a subíndices; sin una selección, activa la conversión de los dígitos que se escriban después de un símbolo o grupo químico. Las fórmulas no reconocidas muestran el motivo y la posición del error. El análisis es informativo: no modifica el inventario, no descubre automáticamente una sustancia nueva ni altera el resultado de una reacción.

### Comparación de rutas de síntesis

Permite seleccionar cualquier compuesto que aparezca como producto en una reacción del catálogo y comparar todas sus rutas:

- Cuando hay rutas alternativas documentadas, muestra sus fichas científicas completas y el autómata general ramificado.
- Cuando un compuesto tiene una sola ruta en el catálogo, también aparece: se muestra su ficha de proceso y el autómata con esa única rama.
- Una reacción que produce varios compuestos puede aparecer como ruta de cada producto correspondiente, incluidos los coproductos.

Antes de las fichas se presenta un autómata general: un inicio común se ramifica por cada ruta comparada y cada rama muestra los estados y las acciones del autómata real correspondiente, convergiendo en el producto objetivo. El diagrama se obtiene de las transiciones alcanzables de `FabricaAutomata`. Para rutas sin una ficha científica específica se genera una descripción a partir de la reacción registrada. La pantalla indica si cada ruta está disponible como práctica ejecutable. Comparar no inicia ni modifica experimentos.

## Historial expresado como química

El historial del cuaderno conserva los intentos y sus resultados. Al abrir un registro, la pestaña **Ruta** vuelve a mostrar el diagrama del autómata que corresponde a la reacción; la pestaña **Etapas** conserva el recorrido en lenguaje químico, las operaciones de laboratorio, el estado de finalización y los productos obtenidos.

Los nombres técnicos de las acciones se traducen a expresiones comprensibles, como “mezclar reactivos”, “activar plasma” y “recuperar el producto”. Los estados registrados se muestran como “reactivos preparados”, “reacción en curso” o “producto sintetizado” cuando corresponda.

## Cómo funciona por debajo

### Protocolos y autómatas

`GramaticasProtocolos` contiene las definiciones regulares de `R_H2O` y `R_DIAMANTE_CVD`. Una regla de protocolo representa la transición entre dos etapas asociada a una acción del laboratorio. `CompiladorGramaticaRegular` genera con esas reglas el `AutomataQuimico` que usa la aplicación. Para los demás procesos del catálogo, `FabricaAutomata` proporciona el autómata que alimenta la guía. Así no se mantiene una lógica formal para mostrar y otra distinta para jugar.

Al ejecutar una acción:

1. La estación permite al jugador realizar la interacción física requerida.
2. La acción llega a `SesionJuego` y al ejecutor de la reacción.
3. El autómata comprueba si la acción es válida desde el estado actual. Una acción no habilitada no puede hacer avanzar la reacción.
4. El motor químico evalúa la disponibilidad de reactivos, las condiciones del experimento y los productos.
5. La sesión registra la transición y notifica a la interfaz, que refresca la guía y el historial.

No todas las reacciones están definidas desde una gramática: las demás continúan usando las transiciones declaradas en `FabricaAutomata`. En ambos casos el jugador realiza las acciones químicas del juego y el estado se valida con el autómata correspondiente.

### Fórmulas

`AnalizadorFormulaQuimica` reconoce elementos, subíndices y grupos entre paréntesis anidados. Convierte subíndices Unicode para el análisis, valida símbolos de elementos químicos y calcula el conteo aplicando correctamente los multiplicadores de cada grupo.

Sus límites actuales son 128 caracteres, 16 niveles de anidamiento y un millón de átomos por elemento. No admite coeficientes estequiométricos externos, cargas iónicas, hidratos, puntos de adición ni anotaciones de fase.

### Responsabilidades de componentes

| Componente | Responsabilidad |
| --- | --- |
| `VistaJuego2D` | Abrir el cuaderno, mostrar inventario e historial, y conectar el juego con el cuaderno químico. |
| `VistaCuadernoQuimico` | Mostrar los protocolos de todos los procesos del catálogo, analizar muestras y comparar productos/rutas. |
| `GramaticasProtocolos` | Mantener las definiciones fuente de secuencias para los protocolos compatibles. |
| `CompiladorGramaticaRegular` | Convertir una definición formal a estados y transiciones del autómata químico. |
| `AutomataQuimico` | Aceptar o rechazar acciones conforme al estado actual del procedimiento. |
| `SesionJuego` | Orquestar acciones, estados, registro de intentos y cambios del experimento. |
| `MotorQuimico` | Evaluar la reacción y determinar los productos químicos. |
| `ProtocoloProcedimiento` | Proporcionar indicaciones de manejo, equipo y rutas químicas comparativas. |
| `AnalizadorFormulaQuimica` | Calcular la composición de una fórmula sin modificar la sesión del juego. |

## Alcance y límites del contenido

- Las descripciones detalladas de manejo están disponibles solo para los procesos cubiertos por `ProtocoloProcedimiento`; los demás muestran los estados y las acciones reales del autómata con información experimental genérica.
- La comparación incluye todos los productos del catálogo de reacciones. Las rutas alternativas documentadas conservan sus fichas; otras rutas se generan desde las reacciones registradas. Las rutas sin reacción asociada al catálogo pueden mostrarse como informativas y se marcan como no ejecutables.
- El análisis de una fórmula calcula composición; no prueba una reacción ni sustituye la disponibilidad de reactivos o las condiciones experimentales.
- La interfaz del jugador no necesita mencionar teoría formal. La documentación técnica del diseño y la relación completa entre la química y la validación están en [GRAMATICAS_Y_AUTOMATAS.md](GRAMATICAS_Y_AUTOMATAS.md).

## Validación

La integración se verifica compilando el proyecto y ejecutando todas las pruebas con JDK 25:

```powershell
$env:JAVA_HOME = 'C:\Users\klmxl\.jdk\jdk-25.0.4.1+1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn clean test
```

La validación ejecutada terminó correctamente: 218 pruebas, sin fallos, errores ni omisiones.
