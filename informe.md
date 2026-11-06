# ChemLab: diseño e implementación de un laboratorio químico educativo en 2D

## Portada

**Título:** ChemLab: diseño e implementación de un laboratorio químico educativo en 2D  
**Autores:** [Completar nombres de los integrantes]  
**Institución:** [Completar institución]  
**Asignatura:** [Completar asignatura]  
**Docente:** [Completar nombre]  
**Lugar y fecha:** [Completar lugar], 31 de octubre de 2026

> **Criterio de presentación:** Como la solicitud no especificó cuál norma debía aplicarse, este borrador usa APA, 7.ª edición, para citas y referencias. Deben ajustarse portada, márgenes, interlineado y demás requisitos a la guía de la institución si esta establece reglas propias.

## Resumen ejecutivo

ChemLab es una aplicación de escritorio educativa que representa prácticas químicas como una experiencia interactiva de laboratorio en 2D. Su propósito de diseño es conectar conceptos químicos —sustancias, fórmulas, reactivos, productos, condiciones y secuencias de operaciones— con acciones contextualizadas en un laboratorio virtual. La aplicación está construida en Java 25 con JavaFX y Maven; las reglas químicas y el estado de los experimentos se mantienen separados de la representación gráfica.

El proyecto contiene un catálogo JSON con 36 sustancias y 25 reacciones, un dominio que representa inventarios, equipos, experimentos, condiciones y resultados, un motor de evaluación química, autómatas para validar el orden de las operaciones y una interfaz jugable con puestos de trabajo, almacén e inventario, historial y cuaderno químico. El cuaderno integra consulta de protocolos, análisis de fórmulas y comparación de rutas de síntesis. Las gramáticas regulares se usan internamente para construir algunos protocolos; no se presentan al jugador como requisito de aprendizaje formal.

El estudio se realizó mediante revisión documental y estática del código, recuento de archivos y datos del catálogo, y ejecución de las pruebas automatizadas. En la ejecución de `mvn test` efectuada el 31 de octubre de 2026 se reportaron 249 pruebas, sin fallos, errores ni omisiones. Este resultado verifica los casos automatizados incluidos en el proyecto; no demuestra por sí solo eficacia pedagógica, precisión experimental completa ni funcionamiento en todos los equipos. El informe reconstruye la secuencia de construcción a partir de dependencias y responsabilidades observables en el código, no a partir de un historial cronológico de versiones.

**Palabras clave:** química, simulación educativa, JavaFX, autómatas finitos, estequiometría, arquitectura de software.

## 1. Introducción

### 1.1 Contexto

La enseñanza de la química requiere relacionar distintos niveles de representación: composición de la materia, fórmulas, ecuaciones, condiciones de reacción y procedimientos experimentales. Las fórmulas expresan proporciones de átomos y las ecuaciones químicas representan transformaciones entre reactivos y productos; interpretar una reacción también requiere considerar cantidades y condiciones (International Union of Pure and Applied Chemistry [IUPAC], s. f.; OpenStax, 2019).

ChemLab aborda este reto como un laboratorio virtual jugable. La persona explora un espacio 2D, selecciona una práctica, se desplaza a estaciones y ejecuta acciones de laboratorio. La aplicación valida dichas acciones mediante el estado del experimento y el autómata correspondiente, mientras que el motor químico evalúa reactivos, condiciones y productos. El cuaderno ofrece información complementaria sin exponer los mecanismos formales como tarea para el jugador (Proyecto ChemLab, s. f.-d, s. f.-e).

**[Insertar Figura 1 aquí: mapa conceptual que conecte sustancias y fórmulas → reacción y estequiometría → protocolo de operaciones → resultado registrado. La figura debe representar la relación entre el universo químico y los componentes de ChemLab.]**

### 1.2 Problemática

La problemática abordada por el diseño puede formularse así: **¿cómo representar las relaciones entre fórmulas, sustancias, reacciones, cantidades y procedimientos de laboratorio en una experiencia digital en la que el usuario comprenda qué hacer y por qué, sin confundir la interfaz con las reglas científicas que validan la práctica?**

Una presentación exclusivamente textual de fórmulas y pasos puede separar el contenido químico de las decisiones operativas: qué reactivo cargar, en qué equipo realizar una operación, qué condición se requiere y cómo se reconoce un resultado. A la vez, si las reglas se implementan dentro de la pantalla, se dificulta probarlas y mantenerlas sin acoplarlas al diseño visual. El código y la documentación del proyecto evidencian que ChemLab intenta responder a ambos aspectos mediante una experiencia espacial y una separación entre interfaz, sesión de juego, autómatas, motor químico y catálogo.

Esta formulación describe la motivación inferida del proyecto. No se encontró en los materiales revisados un estudio de usuarios, una medición de aprendizaje ni una evaluación comparativa que pruebe que la aplicación resuelve empíricamente el problema.

### 1.3 Objetivos

**Objetivo general.** Describir y analizar ChemLab como laboratorio químico educativo en 2D, explicando cómo modela el dominio químico, organiza su arquitectura y transforma protocolos de reacción en interacciones de juego.

**Objetivos específicos**

1. Identificar las entidades químicas y los datos que integran el catálogo.
2. Explicar el flujo desde la selección de una práctica hasta la evaluación y registro del resultado.
3. Describir la relación entre gramáticas regulares, autómatas químicos y validación de acciones.
4. Analizar la organización de los componentes de interfaz, simulación y dominio.
5. Resumir la evidencia de pruebas automatizadas y delimitar lo que esta evidencia no permite concluir.

### 1.4 Alcance

El análisis cubre el código Java de producción y pruebas, el archivo Maven, los recursos JSON y CSS y la documentación técnica presente en el proyecto. Se describe el estado visible en el código y se distingue de las inferencias sobre su motivación o secuencia de desarrollo. No incluye una evaluación didáctica con estudiantes, una auditoría de exactitud experimental, un análisis de seguridad de laboratorio real ni mediciones de rendimiento.

## 2. Marco teórico o referencial

### 2.1 Representación química

Una sustancia se identifica en ChemLab mediante datos como identificador, nombre, fórmula, tipo, estado de la materia y masa molar. Una reacción asocia reactivos y productos con coeficientes, tipo de transformación y condiciones requeridas. El inventario representa cantidades y unidades, mientras que las condiciones experimentales conservan temperatura, presión, pH y atmósfera. Este modelo da soporte a la distinción entre conocer una fórmula y comprobar si una reacción puede ejecutarse con el material y las condiciones disponibles.

La estequiometría relaciona las proporciones de reactivos y productos de acuerdo con la ecuación de una reacción. En el proyecto, el cálculo de cantidades se delega a componentes del motor químico, en vez de incorporarse a la capa de dibujo. El catálogo existente es finito y de alcance didáctico: registra 36 sustancias y 25 reacciones, por lo que no debe interpretarse como una base exhaustiva de la química.

### 2.2 Gramáticas regulares y autómatas

Una práctica puede verse como una secuencia ordenada de acciones. En teoría de la computación, una gramática regular puede describir secuencias aceptables y un autómata finito puede reconocerlas mediante estados y transiciones (Hopcroft et al., 2006). ChemLab adapta esta idea: los estados representan etapas del protocolo y las transiciones se etiquetan con acciones del laboratorio.

El proyecto define gramáticas fuente para la síntesis de agua y el crecimiento de diamante CVD. `CompiladorGramaticaRegular` transforma esas definiciones en `AutomataQuimico`; otras prácticas mantienen autómatas definidos en `FabricaAutomata`. Esta distinción es importante: el autómata comprueba el orden de las acciones, pero no sustituye el cálculo químico. El motor conserva la responsabilidad de evaluar si hay reactivos suficientes, si se cumplen condiciones y qué productos se obtienen (Proyecto ChemLab, s. f.-d).

### 2.3 Simulación educativa y aprendizaje situado

Una simulación puede organizar contenidos y acciones alrededor de un escenario, permitiendo que el usuario explore representaciones y reciba información contextual. En ChemLab, las acciones se ubican en un laboratorio virtual: acercarse a una estación, cargar sustancias, accionar una herramienta y consultar el cuaderno. Esta forma de interacción ofrece un contexto para presentar contenido químico; no constituye por sí misma evidencia de aprendizaje, que requeriría objetivos medibles, instrumentos de evaluación y pruebas con usuarios.

### 2.4 Arquitectura de software

La arquitectura del proyecto separa responsabilidades en paquetes de modelo, motor, autómatas, catálogos, fábricas, repositorios, servicios, sesión de juego y UI. La separación busca que la presentación visual consuma el estado del dominio sin modificar directamente las reglas químicas. El proyecto también usa mecanismos reconocibles como fábricas para crear experimentos/sesiones y observadores para notificar cambios de sesión a la interfaz. Estos mecanismos están presentes en el código; su conveniencia final debe valorarse por su uso y por el costo de mantenimiento, no solo por el nombre del patrón (Gamma et al., 1994; Proyecto ChemLab, s. f.-c).

**[Insertar Figura 2 aquí: diagrama de paquetes/capas. Mostrar `resources` y catálogo JSON como fuente de datos; `model`, `grammar`, `automata` y `engine` como dominio y reglas; `game` como coordinación; `ui` como presentación; y `factory`, `repository` y `service` como componentes de creación, acceso y conversión.]**

## 3. Metodología

### 3.1 Tipo de estudio

Se realizó un estudio descriptivo de caso con análisis documental y estático del software. La unidad de análisis es el proyecto ChemLab presente en el directorio de trabajo. El procedimiento se diseñó para describir la implementación observable, no para reconstruir decisiones históricas no documentadas.

### 3.2 Fuentes y procedimiento

El análisis se efectuó en los siguientes pasos, mediante inspección del código fuente, las pruebas y los documentos incluidos en el proyecto (Proyecto ChemLab, s. f.-b):

1. **Revisión de la estructura:** identificación de archivos Maven, código de producción, pruebas, recursos y documentos del proyecto.
2. **Lectura del universo químico:** inspección de las clases de sustancias, reacciones, cantidades, inventarios, equipos, condiciones y resultados.
3. **Seguimiento de reglas:** revisión de la carga del catálogo, creación de experimentos, autómatas, ejecución de acciones y cálculo de resultados.
4. **Seguimiento de la interfaz:** lectura del punto de entrada, pantalla 2D, renderer, puestos de trabajo y cuaderno químico.
5. **Verificación cuantitativa:** recuento de entradas del catálogo y ejecución de `mvn test`.
6. **Síntesis:** organización de hallazgos en una secuencia lógica desde el dominio químico hasta la experiencia visible.

La secuencia de construcción que se presenta en resultados es una **reconstrucción lógica** basada en dependencias entre componentes. No se dispone aquí de evidencia suficiente para afirmar que esa fue exactamente la cronología en que el equipo desarrolló cada parte.

### 3.3 Evidencia y límites metodológicos

Se identificaron 106 archivos Java bajo `src/main/java` y 45 archivos Java bajo `src/test/java`. Los recursos incluyen un catálogo químico JSON y hojas de estilos CSS. La ejecución de `mvn test`, realizada el 31 de octubre de 2026, terminó con `BUILD SUCCESS`: 249 pruebas ejecutadas, 0 fallos, 0 errores y 0 omitidas.

La evidencia de pruebas corresponde a los casos automatizados del repositorio. No equivale a cobertura total, prueba de usabilidad, validación de todas las sustancias contra fuentes experimentales ni garantía de que la aplicación se inicie correctamente en cualquier sistema. No se ejecutó una evaluación visual de la interfaz en una ventana de escritorio.

## 4. Resultados

### 4.1 Universo químico representado

El catálogo JSON registra 36 sustancias y 25 reacciones (Proyecto ChemLab, s. f.-a). Entre las sustancias están elementos como hidrógeno, oxígeno, carbono, hierro, sodio, cloro, azufre, cobre, calcio, silicio, magnesio, aluminio, fósforo y níquel; además de compuestos y materiales como agua, dióxido de carbono, amoníaco, cloruro de sodio, ácido clorhídrico, óxidos, diamante, grafito, solución salina y productos de corrosión. Las reacciones incluyen síntesis, combustión, oxidación, corrosión, precipitación, calcinación, disolución, separación por evaporación y rutas de producción de diamante.

La aplicación modela las sustancias como datos del dominio y las relaciones de reacción como mapas de reactivos y productos con coeficientes. Las condiciones se expresan mediante rangos o requisitos asociados a las reacciones. El equipo y las acciones se representan con tipos enumerados y objetos del juego; el JSON consultado no contiene un arreglo de equipos.

El análisis de fórmulas dispone de una tabla periódica propia para validar símbolos y componer los resultados del parser. La documentación describe límites concretos para el análisis y las reglas de nomenclatura, por lo que el cuaderno debe entenderse como una herramienta informativa con alcance acotado, no como nomenclador químico general.

**[Insertar Figura 3 aquí: extracto visual del catálogo o esquema de entidades. Debe mostrar las relaciones `Sustancia`–`Reaccion`–`Inventario`–`Experimento` y señalar que la aplicación contiene un catálogo limitado de prácticas.]**

### 4.2 Construcción lógica del proyecto, paso a paso

La siguiente secuencia explica cómo las partes del programa se conectan. Se presenta como orden conceptual de dependencias, no como cronología confirmada.

#### Paso 1. Definir qué entidades existen en el laboratorio

El punto de partida del universo químico es traducir conceptos a objetos: `Sustancia`, `Reaccion`, `Experimento`, `Inventario`, `Equipo`, `CondicionesExperimentales`, `Objetivo`, `ResultadoQuimico` y registros del cuaderno. Las enumeraciones acotan categorías como estado de materia, tipo de equipo, tipo de sustancia y acciones de laboratorio.

Esta etapa permite que el resto del sistema use objetos consistentes en lugar de cadenas sueltas para representar cada material o estado. Por ejemplo, la reacción relaciona reactivos y productos; el experimento concentra el inventario, equipo, condiciones y estado de ejecución.

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/model/entity/Sustancia.java`, líneas 17–67; `src/main/java/com/chemlab/model/entity/Reaccion.java`, líneas 10–145; `src/main/java/com/chemlab/model/entity/Experimento.java`, líneas 9–116.]**

#### Paso 2. Registrar sustancias y reacciones como datos

El archivo `catalogo-quimico.json` funciona como fuente de datos del catálogo. `RepositorioCatalogoQuimicoJson` interpreta la información y construye los objetos que utiliza `CatalogoQuimico`. De esta manera, ampliar o corregir una entrada del catálogo no obliga necesariamente a alterar el código del motor.

El recuento del archivo disponible produce 36 sustancias y 25 reacciones. El catálogo es, por tanto, un conjunto explícito de experiencias seleccionadas. No se debe inferir de estos datos que se modelen todas las sustancias, reacciones o equipos posibles.

**[Mostrar código, sin copiarlo en este informe: `src/main/resources/catalogo-quimico.json`, líneas 1–573; `src/main/java/com/chemlab/repository/RepositorioCatalogoQuimicoJson.java`, líneas 21–185; `src/main/java/com/chemlab/catalogo/CatalogoQuimico.java`, líneas 10–35.]**

#### Paso 3. Expresar condiciones y cantidades de los experimentos

Las clases de dominio representan cantidades y condiciones de forma explícita. Los objetos de `FabricaExperimento` preparan los datos necesarios para iniciar prácticas: la reacción, los materiales iniciales y las condiciones o equipos previstos. Esto separa la definición de una práctica de la interacción paso a paso del jugador.

#### Paso 4. Definir qué acciones son válidas

Los protocolos tienen etapas y acciones. En las reacciones que usan gramáticas, `GramaticasProtocolos` declara las secuencias y `CompiladorGramaticaRegular` construye un autómata. `FabricaAutomata` reúne la selección de autómatas por reacción, incluidos los protocolos definidos directamente como estados y transiciones.

Cuando el jugador intenta realizar una acción, el autómata valida si existe una transición habilitada desde el estado actual. Una acción no permitida no debe avanzar el flujo formal. Esta regla preserva el orden del protocolo, sin delegar la evaluación de productos al autómata.

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/grammar/GramaticasProtocolos.java`, líneas 10–60; `src/main/java/com/chemlab/grammar/CompiladorGramaticaRegular.java`, líneas 16–54; `src/main/java/com/chemlab/automata/AutomataQuimico.java`, líneas 17–55 y 219–335; `src/main/java/com/chemlab/factory/FabricaAutomata.java`, líneas 23–60 y 539–566.]**

#### Paso 5. Evaluar la química y aplicar el resultado

El motor químico coordina la evaluación estequiométrica, las reglas de reacción, el cálculo de productos y la actualización del inventario. Si la evaluación no es posible o la regla de reacción falla, el motor no aplica los cambios como si la operación hubiera tenido éxito. Así se distingue la validez del procedimiento (autómata) de la viabilidad de la transformación química (motor).

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/engine/MotorQuimico.java`, líneas 19–106; y, si se explica el cálculo en mayor detalle, `src/main/java/com/chemlab/engine/CalculadorEstequiometrico.java`, desde la declaración de la clase hasta el método principal de evaluación.]**

#### Paso 6. Coordinar la práctica en una sesión

`SesionJuego` coordina el experimento en curso, la reacción, el autómata, el modo de juego, las acciones registradas, las pistas, los productos y el historial. También notifica a los observadores cuando se inicia, progresa, completa, falla o se abandona un experimento.

En un flujo representativo, la sesión inicia el experimento, recibe una acción, solicita su evaluación al ejecutor del autómata y, al completar el proceso, registra el resultado y los productos descubiertos en el cuaderno. Esto permite que la interfaz reaccione a cambios sin ser dueña de las reglas químicas.

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/game/SesionJuego.java`, líneas 74–210; y el bloque de finalización/registro del experimento desde el método que completa la reacción.]**

#### Paso 7. Presentar el laboratorio como espacio navegable

`Main` inicia JavaFX y crea `VistaJuego2D`. Esta vista reúne el mapa, el jugador, el HUD, los controles y los paneles. `RenderizadorLaboratorio2D` se encarga del dibujo del mapa, el personaje, las estaciones y los efectos visibles. La entrada se interpreta en la vista y se convierte en acciones del dominio; el renderer no es el componente encargado de calcular la química.

El usuario puede desplazarse con WASD o flechas, interactuar con una estación mediante F, abrir el cuaderno con E y abrir el almacén o selector de prácticas con M. Escape cierra el panel o menú actual. Estos controles se documentan en el README del proyecto.

**[Insertar Figura 4 aquí: captura de la vista general del laboratorio 2D. Señalar mapa, personaje, estaciones, HUD y teclas disponibles.]**

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/Main.java`, líneas 20–36; `src/main/java/com/chemlab/ui/game/VistaJuego2D.java`, líneas 74–212 y 280–410; `src/main/java/com/chemlab/ui/game/RenderizadorLaboratorio2D.java`, líneas 21–110 y 119–235.]**

#### Paso 8. Representar la interacción en puestos de trabajo

Al llegar al puesto necesario para el paso activo, `VistaPuestoLaboratorio` presenta el equipo y las herramientas. El usuario puede cargar reactivos en el recipiente dibujado y accionar controles de la estación. `PlanificadorEstacionesProcedimiento` ayuda a asociar etapas con equipos y a determinar cuándo el procedimiento requiere trasladarse a otra estación. `VisualizadorReaccion` mantiene información de representación visual —como perfiles, especies y fenómenos— separada del cálculo de la reacción.

**[Insertar Figura 5 aquí: captura de un puesto de trabajo con el reactivo, aparato, mando interactivo, etapa del procedimiento y condiciones mostradas. No usar una imagen de un laboratorio real como si fuera el simulador.]**

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/ui/workbench/VistaPuestoLaboratorio.java`, líneas 43–174; y las rutinas de dibujo del equipo y de la transformación, líneas 702–834 y 927–1126; `src/main/java/com/chemlab/game/procedure/PlanificadorEstacionesProcedimiento.java`, líneas 12–60.]**

#### Paso 9. Consultar el cuaderno y analizar fórmulas

El cuaderno químico ofrece tres áreas: protocolos de síntesis, análisis de muestras y comparación de rutas. Los protocolos se vinculan con autómatas alcanzables del catálogo; el analizador de fórmulas reconoce elementos y grupos, calcula cantidades de átomos y entrega errores para entradas inválidas. La nomenclatura implementada aplica reglas limitadas y el proyecto indica cuando una familia química queda fuera del alcance.

El análisis de fórmulas es informativo: no inicia reacciones ni altera el inventario. El cuaderno también permite revisar el historial de intentos y la ruta seguida por el autómata.

**[Insertar Figura 6 aquí: captura del cuaderno químico en la sección de análisis. Debe mostrar la fórmula, composición por elementos y el árbol o explicación del análisis.]**

**[Mostrar código, sin copiarlo en este informe: `src/main/java/com/chemlab/ui/notebook/VistaCuadernoQuimico.java`, líneas 49–200 para protocolos, 246–370 para análisis y 504–658 para comparación; `src/main/java/com/chemlab/grammar/formula/AnalizadorFormulaQuimica.java`, líneas 12–235.]**

#### Paso 10. Verificar reglas con pruebas automatizadas

El proyecto incluye pruebas para entidades, cantidades, catálogos, reglas químicas, cálculo estequiométrico, estados y transiciones de autómatas, fábricas, sesión de juego, integración de experimentos, nomenclatura, conversión de unidades y persistencia JSON. Esta organización prueba varios componentes de forma aislada y algunas interacciones entre capas.

**[Mostrar código, sin copiarlo en este informe: seleccionar un caso de `src/test/java/com/chemlab/engine/CalculadorEstequiometricoTest.java`, un caso de `src/test/java/com/chemlab/automata/AutomataQuimicoTest.java` y uno de `src/test/java/com/chemlab/game/ExperimentosCatalogoIntegracionTest.java`. Indicar los nombres exactos de las pruebas que se expliquen.]**

### 4.3 Arquitectura y flujo de ejecución

La organización funcional observada puede resumirse así:

| Componente | Responsabilidad principal |
|---|---|
| `model` | Entidades, valores, estados y excepciones del dominio químico. |
| `catalogo` | Consulta y organización del catálogo químico. |
| `repository` | Carga del catálogo JSON y persistencia de experimentos en JSON. |
| `grammar` | Definición y compilación de protocolos; análisis de fórmulas. |
| `automata` | Estados, transiciones y validación del orden de acciones. |
| `engine` | Evaluación de condiciones, estequiometría, reacciones y productos. |
| `factory` | Construcción de sesiones, experimentos y autómatas. |
| `game` | Sesión de juego, mapa, jugador, estaciones y planificación de protocolos. |
| `service` | Conversión química y de unidades. |
| `ui` | Pantallas JavaFX, cuaderno, renderer, estaciones y controles. |

El flujo general es: **catálogo → selección de práctica → creación de experimento y autómata → sesión → acción del jugador → validación del autómata → evaluación del motor químico → actualización de inventario/resultado → notificación a UI → registro en cuaderno e historial**.

**[Insertar Figura 7 aquí: diagrama de secuencia de una práctica, desde seleccionar la reacción hasta registrar el producto. Mostrar por separado la decisión del autómata y la evaluación química.]**

### 4.4 Tecnologías y configuración

El archivo `pom.xml` declara Java 25, JavaFX 25.0.5, Maven Compiler Plugin 3.14.0, JavaFX Maven Plugin 0.0.8, JUnit Jupiter 5.12.2, Jackson Databind 2.22.2 y Surefire 3.5.3 (Apache Maven, s. f.; JUnit Team, s. f.; OpenJFX, s. f.; Oracle, 2025). La aplicación es un proyecto Maven de un solo módulo. El README recomienda JDK 25 y Maven 3.9 o posterior; la prueba ejecutada en este análisis terminó correctamente con Maven 3.8.8, versión inferior a la recomendación documental.

**[Mostrar código, sin copiarlo en este informe: `pom.xml`, líneas 1–65. Enfatizar las propiedades de versión y los plugins de compilación, ejecución JavaFX y pruebas.]**

### 4.5 Resultados de verificación

La ejecución observada de `mvn test` terminó con `BUILD SUCCESS` y reportó 249 pruebas, cero fallos, cero errores y cero omitidas. Este resultado es coherente con la presencia de 45 archivos de prueba y de casos en varias áreas del dominio y la aplicación.

Se encontró una diferencia documental que debe resolverse antes de entregar una versión final: `Gramaticas a quimica.md` indica que una validación anterior tuvo 222 pruebas, mientras que la ejecución del 31 de octubre de 2026 registró 249. Para este informe se cita el resultado de la ejecución actual; la cifra histórica del documento debe actualizarse o contextualizarse para evitar contradicciones.

## 5. Discusión

### 5.1 Aportes observables

El principal aporte arquitectónico es mantener distintas responsabilidades para la interfaz, el proceso de juego y la química. La interfaz presenta estados y controles; la sesión coordina la práctica; el autómata impone el orden del procedimiento; el motor evalúa los reactivos, condiciones y productos. Esta separación facilita comprender y probar cada regla con menor dependencia del renderer.

El catálogo externo permite representar sustancias y reacciones como datos, mientras que las pruebas del repositorio cubren reglas químicas y estructuras de control. El cuaderno une la práctica con explicaciones consultables: muestra protocolos, analiza fórmulas y organiza rutas de síntesis. La interfaz no exige al usuario estudiar formalismos matemáticos para ejecutar el experimento, aunque esos formalismos sean útiles internamente para validar algunas secuencias.

### 5.2 Límites y aspectos que requieren atención

1. **Alcance del catálogo.** Las 36 sustancias y 25 reacciones constituyen una selección cerrada; la cobertura de procesos no debe presentarse como representación completa de la química.
2. **Alcance del modelo científico.** El sistema computa reglas expresamente codificadas. La existencia de temperaturas, presiones o coeficientes en el modelo no demuestra por sí sola que todas las condiciones sean suficientes para describir experimentalmente una reacción real.
3. **Alcance del análisis de fórmulas.** El parser y la nomenclatura cubren las reglas documentadas por el proyecto. Los casos no implementados deben seguir señalándose como fuera de alcance, en lugar de producir nombres o resultados especulativos.
4. **Evaluación educativa pendiente.** Las pruebas de software verifican comportamientos programados, no si estudiantes retienen conceptos o mejoran su desempeño. Para sostener una afirmación pedagógica se requieren objetivos de aprendizaje, instrumentos y evaluación con participantes.
5. **Validación visual pendiente.** La ejecución automatizada de pruebas no sustituye una sesión de observación de la interfaz en sistemas operativos y tamaños de pantalla distintos.
6. **Coherencia documental.** La discrepancia entre los conteos de 222 y 249 pruebas indica que las instrucciones o informes de ejecución requieren mantenimiento.
7. **Verificación en laboratorio real.** ChemLab es una simulación educativa y no debe usarse como sustituto de protocolos institucionales, supervisión docente ni prácticas de seguridad química.

### 5.3 Mejoras recomendadas

- Definir resultados de aprendizaje observables y evaluar la aplicación con usuarios mediante tareas guiadas y rúbricas.
- Documentar la fuente científica y el alcance de cada dato de reacción, condición y nomenclatura.
- Mantener una única cifra vigente de pruebas en la documentación y actualizarla automáticamente o evitar cifras fijas cuando cambien con frecuencia.
- Añadir pruebas de integración de extremo a extremo para los flujos principales de interfaz, además de las pruebas unitarias ya presentes.
- Revisar periódicamente las versiones de Java, JavaFX y dependencias, y comprobar en una máquina limpia las instrucciones de compilación y ejecución.
- Señalar dentro de la interfaz qué contenidos son simplificaciones didácticas y qué conclusiones no pueden extrapolarse a un laboratorio real.

## 6. Conclusiones

ChemLab integra un catálogo químico, un modelo de dominio, protocolos de acciones, autómatas finitos, un motor de evaluación y una interfaz de laboratorio 2D. El proyecto transforma una práctica química en una secuencia que puede recorrerse, valida el orden de las acciones y, por separado, calcula si las reglas químicas codificadas permiten obtener productos.

La arquitectura está organizada para mantener la presentación desacoplada de las reglas del dominio. La carga de datos JSON y la estructura de paquetes favorecen la extensión del catálogo y el mantenimiento de componentes. El cuaderno químico amplía la práctica con consulta de protocolos, análisis de muestras y comparación de rutas, siempre dentro del alcance implementado.

La ejecución de `mvn test` del 31 de octubre de 2026 aporta evidencia positiva sobre los 249 casos automatizados incluidos en ese momento: no registró fallos, errores ni pruebas omitidas. Sin embargo, no prueba efectividad educativa, exactitud exhaustiva ni funcionamiento gráfico en todos los entornos. La conclusión responsable es que ChemLab constituye una base funcional de simulación química educativa con una separación clara entre reglas y presentación, cuyo siguiente paso de validación debería incluir evaluación pedagógica, revisión científica de los datos y pruebas de la experiencia visual.

## 7. Referencias bibliográficas

Apache Maven. (s. f.). *Maven documentation*. https://maven.apache.org/guides/

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.

Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2006). *Introduction to automata theory, languages, and computation* (3.ª ed.). Pearson.

International Union of Pure and Applied Chemistry. (s. f.). *Compendium of chemical terminology (the Gold Book)*. Recuperado el 31 de octubre de 2026, de https://goldbook.iupac.org/

JUnit Team. (s. f.). *JUnit 5 user guide*. https://junit.org/junit5/docs/current/user-guide/

OpenJFX. (s. f.). *OpenJFX documentation*. Recuperado el 31 de octubre de 2026, de https://openjfx.io/

OpenStax. (2019). *Chemistry 2e*. Rice University. https://openstax.org/details/books/chemistry-2e

Oracle. (2025). *Java Platform, Standard Edition 25 documentation*. https://docs.oracle.com/en/java/javase/25/

Proyecto ChemLab. (s. f.-a). *Catálogo químico de ChemLab* [Archivo JSON].

Proyecto ChemLab. (s. f.-b). *Código fuente y pruebas automatizadas de ChemLab* [Software de computadora].

Proyecto ChemLab. (s. f.-c). *Estructura de ChemLab* [Documento de trabajo].

Proyecto ChemLab. (s. f.-d). *Gramáticas y autómatas: implementación interna* [Documento de trabajo].

Proyecto ChemLab. (s. f.-e). *README del proyecto* [Documento de trabajo].

## Anexo A. Ubicación sugerida del código que se explicará en el informe

Las siguientes referencias indican qué fragmentos mostrar. No se incluye código en este documento.

| Tema explicado | Archivo y líneas sugeridas |
|---|---|
| Punto de entrada JavaFX | `src/main/java/com/chemlab/Main.java`, líneas 20–36 |
| Versiones y dependencias | `pom.xml`, líneas 1–65 |
| Registro de sustancias y reacciones | `src/main/resources/catalogo-quimico.json`, líneas 1–573 |
| Entidades químicas | `src/main/java/com/chemlab/model/entity/Sustancia.java`, líneas 17–67; `Reaccion.java`, líneas 10–145; `Experimento.java`, líneas 9–116 |
| Lectura JSON | `src/main/java/com/chemlab/repository/RepositorioCatalogoQuimicoJson.java`, líneas 21–185 |
| Reglas y cálculo químico | `src/main/java/com/chemlab/engine/MotorQuimico.java`, líneas 19–106 |
| Gramática y compilación | `src/main/java/com/chemlab/grammar/GramaticasProtocolos.java`, líneas 10–60; `CompiladorGramaticaRegular.java`, líneas 16–54 |
| Transiciones de autómata | `src/main/java/com/chemlab/automata/AutomataQuimico.java`, líneas 219–335 |
| Coordinación de sesión | `src/main/java/com/chemlab/game/SesionJuego.java`, líneas 134–260 |
| Entrada y movimiento del jugador | `src/main/java/com/chemlab/ui/game/VistaJuego2D.java`, líneas 153–212 y 280–410 |
| Renderizado del laboratorio | `src/main/java/com/chemlab/ui/game/RenderizadorLaboratorio2D.java`, líneas 21–110 y 119–235 |
| Estación de trabajo | `src/main/java/com/chemlab/ui/workbench/VistaPuestoLaboratorio.java`, líneas 43–174 y 702–834 |
| Cuaderno químico | `src/main/java/com/chemlab/ui/notebook/VistaCuadernoQuimico.java`, líneas 124–200, 246–370 y 504–658 |
| Análisis de fórmulas | `src/main/java/com/chemlab/grammar/formula/AnalizadorFormulaQuimica.java`, líneas 12–235 |
| Pruebas representativas | `src/test/java/com/chemlab/engine/CalculadorEstequiometricoTest.java`; `src/test/java/com/chemlab/automata/AutomataQuimicoTest.java`; `src/test/java/com/chemlab/game/ExperimentosCatalogoIntegracionTest.java` |

## Anexo B. Ubicación sugerida de figuras

1. **Figura 1:** mapa conceptual del universo químico y su traducción a componentes de software.
2. **Figura 2:** arquitectura por paquetes y flujo de datos.
3. **Figura 3:** modelo conceptual de sustancias, reacciones, inventarios y experimentos.
4. **Figura 4:** pantalla general del laboratorio 2D.
5. **Figura 5:** estación o mesa de trabajo con instrumentos y controles.
6. **Figura 6:** pantalla del cuaderno en análisis de una muestra.
7. **Figura 7:** diagrama de secuencia desde seleccionar una práctica hasta registrar el producto.

*Todas las figuras anteriores son espacios reservados; este archivo no contiene imágenes.*
