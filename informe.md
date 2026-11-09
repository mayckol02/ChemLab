# ChemLab: diseño e implementación de un laboratorio químico educativo en 2D

## Portada

**Título:** ChemLab: diseño e implementación de un laboratorio químico educativo en 2D  
**Autores:** [Completar nombres de los integrantes]  
**Institución:** [Completar institución]  
**Asignatura:** [Completar asignatura]  
**Docente:** [Completar nombre]  
**Lugar y fecha:** [Completar lugar], 7 de noviembre de 2026

> **Criterio de presentación:** Este borrador usa APA, 7.ª edición, para citas y referencias. Deben ajustarse portada, márgenes, interlineado y demás requisitos a la guía de la institución si establece reglas propias.

## Resumen ejecutivo

ChemLab es una aplicación de escritorio educativa que representa prácticas químicas como una experiencia interactiva de laboratorio en 2D. Está desarrollada con Java 25, JavaFX y Maven. Su diseño separa la interfaz, la coordinación de la partida, el motor químico y los mecanismos que validan el orden de las operaciones.

El catálogo contiene 36 sustancias y 25 reacciones. La definición de cada práctica se encuentra en un archivo JSON independiente, enumerado por el manifiesto `src/main/resources/catalogo-quimico.json`. Estas definiciones incluyen objetivo, equipo, condiciones iniciales, materiales, etapas, fases observables y una gramática de acciones. El sistema compila la gramática declarada en un autómata finito, mientras que el motor químico evalúa las transformaciones y sus resultados. Por ello, ChemLab continúa basándose en gramáticas y autómatas: los JSON declaran las prácticas y el código común las carga, compila y ejecuta.

La aplicación cuenta además con perfiles locales, progreso individual, una ruta opcional de misiones en Modo Difícil, retroalimentación durante la práctica, resúmenes de resultados y alertas educativas no bloqueantes. Los perfiles se guardan en archivos JSON en la carpeta personal del usuario; no se emplea una base de datos.

El análisis se realizó mediante revisión del código, los recursos, la documentación y las pruebas. En la ejecución de `mvn test` efectuada el 7 de noviembre de 2026, Maven reportó 261 pruebas ejecutadas, sin fallos, errores ni omisiones. Esta evidencia verifica los casos automatizados incluidos; no demuestra por sí sola la eficacia pedagógica, la exactitud científica exhaustiva ni el funcionamiento visual en todos los entornos.

**Palabras clave:** química, simulación educativa, JavaFX, autómatas finitos, gramáticas regulares, estequiometría, persistencia JSON.

## 1. Introducción

### 1.1 Contexto

La enseñanza de la química requiere relacionar distintos niveles de representación: composición de la materia, fórmulas, ecuaciones, cantidades, condiciones y procedimientos experimentales. Las fórmulas expresan proporciones de átomos y las ecuaciones químicas representan transformaciones entre reactivos y productos; interpretar una reacción también requiere considerar cantidades y condiciones (International Union of Pure and Applied Chemistry [IUPAC], s. f.; OpenStax, 2019).

ChemLab aborda este reto como un laboratorio virtual jugable. La persona explora un espacio 2D, selecciona una práctica, se desplaza a estaciones y ejecuta acciones de laboratorio. La aplicación valida dichas acciones mediante el estado del experimento y el autómata correspondiente, mientras que el motor químico evalúa reactivos, condiciones y productos. El cuaderno presenta protocolos, análisis de muestras, rutas de síntesis e historial, sin exigir que el jugador estudie formalmente las gramáticas o los autómatas (Proyecto ChemLab, s. f.-d, s. f.-e).

**[Insertar Figura 1 aquí: mapa conceptual que conecte sustancias y fórmulas → reacción y estequiometría → gramática y autómata del protocolo → resultado registrado.]**

### 1.2 Problemática

La problemática abordada por el diseño puede formularse así: **¿cómo representar las relaciones entre fórmulas, sustancias, reacciones, cantidades y procedimientos de laboratorio en una experiencia digital en la que el usuario comprenda qué hacer y por qué, sin confundir la interfaz con las reglas científicas que validan la práctica?**

Una presentación exclusivamente textual puede separar el contenido químico de las decisiones operativas: qué reactivo cargar, en qué equipo realizar una operación, qué condición se requiere y cómo reconocer un resultado. A la vez, si las reglas se implementan dentro de la pantalla, se dificulta probarlas y mantenerlas sin acoplarlas al diseño visual. ChemLab responde desde su arquitectura mediante una experiencia espacial y una separación entre interfaz, sesión de juego, gramáticas, autómatas, motor químico y datos declarativos.

Esta formulación describe la motivación inferida del proyecto. No se encontró un estudio de usuarios, una medición de aprendizaje ni una evaluación comparativa que pruebe que la aplicación resuelve empíricamente el problema.

### 1.3 Objetivos

**Objetivo general.** Describir y analizar ChemLab como laboratorio químico educativo en 2D, explicando cómo modela el dominio químico, organiza su arquitectura y transforma prácticas declarativas en interacciones de juego validadas por gramáticas y autómatas.

**Objetivos específicos**

1. Identificar las entidades químicas y los datos que integran el catálogo.
2. Explicar el flujo desde la selección de una práctica hasta la evaluación y registro del resultado.
3. Describir cómo las gramáticas externas se compilan en autómatas y cómo estos colaboran con el motor químico.
4. Analizar la organización de los componentes de interfaz, simulación, persistencia y dominio.
5. Resumir las funcionalidades implementadas, la evidencia de pruebas automatizadas y los aspectos que todavía requieren trabajo.

### 1.4 Alcance

El análisis cubre el código Java de producción y pruebas, el archivo Maven, los recursos JSON y CSS y la documentación presente en el proyecto. Se describe el estado visible en el código y se distingue de las inferencias sobre su motivación o secuencia de desarrollo. No incluye una evaluación didáctica con estudiantes, una auditoría de exactitud experimental, un análisis de seguridad de laboratorio real ni mediciones de rendimiento.

## 2. Marco teórico o referencial

### 2.1 Representación química

Una sustancia se identifica en ChemLab mediante datos como identificador, nombre, fórmula, tipo, estado de la materia y masa molar. Una reacción asocia reactivos y productos con coeficientes, tipo de transformación y condiciones requeridas. El inventario representa cantidades y unidades, mientras que las condiciones experimentales conservan temperatura, presión, pH y atmósfera.

La estequiometría relaciona las proporciones de reactivos y productos según la ecuación de una reacción. En el proyecto, el cálculo de cantidades se delega a componentes del motor químico, en vez de incorporarse a la capa de dibujo. El catálogo es finito y de alcance didáctico: sus 36 sustancias y 25 reacciones no constituyen una representación exhaustiva de la química.

### 2.2 Gramáticas regulares y autómatas

Una práctica puede modelarse como una secuencia ordenada de acciones. En teoría de la computación, una gramática regular puede describir secuencias aceptables y un autómata finito puede reconocerlas mediante estados y transiciones (Hopcroft et al., 2006). ChemLab adapta esta idea: los estados representan etapas del protocolo y las transiciones se etiquetan con acciones del laboratorio.

En el estado actual, cada archivo de práctica declara su gramática. `CompiladorGramaticaRegular` transforma esa gramática en un `AutomataQuimico`, que valida el orden y el avance de las acciones. La gramática describe las secuencias aceptadas; el autómata mantiene el estado de ejecución. El autómata no sustituye el cálculo químico: el motor conserva la responsabilidad de evaluar si hay reactivos suficientes, si se cumplen las condiciones modeladas y qué productos se obtienen (Proyecto ChemLab, s. f.-a, s. f.-d).

### 2.3 Simulación educativa y aprendizaje situado

Una simulación puede organizar contenidos y acciones alrededor de un escenario, permitiendo que el usuario explore representaciones y reciba información contextual. En ChemLab, las acciones se ubican en un laboratorio virtual: acercarse a una estación, cargar sustancias, accionar una herramienta y consultar el cuaderno. La retroalimentación durante la práctica y el análisis al finalizar relacionan la acción con el estado del proceso.

Este contexto ofrece una forma de presentar contenido químico; no constituye por sí mismo evidencia de aprendizaje. Para sostener conclusiones pedagógicas se requieren objetivos medibles, instrumentos de evaluación y pruebas con usuarios.

### 2.4 Arquitectura de software

La organización del proyecto separa responsabilidades en paquetes de modelo, motor, autómatas, gramáticas, catálogo, fábricas, repositorios, sesión de juego y UI. Los datos de prácticas se cargan desde recursos JSON; los datos de perfiles se persisten en archivos locales fuera de los recursos de la aplicación. La vista observa la sesión y presenta su estado sin ser propietaria de las reglas químicas.

El proyecto utiliza fábricas para construir experimentos y compilar autómatas, repositorios para leer y guardar datos, y observadores para notificar cambios de sesión a la interfaz. Estos mecanismos están presentes en el código; su conveniencia debe valorarse por su uso y por el costo de mantenimiento, no solo por el nombre del patrón (Gamma et al., 1994; Proyecto ChemLab, s. f.-c).

**[Insertar Figura 2 aquí: diagrama de capas. Mostrar JSON de prácticas y catálogo; modelo, gramática, autómata y motor; sesión y coordinación del juego; UI; y repositorios locales de catálogo y perfiles.]**

## 3. Metodología

### 3.1 Tipo de estudio

Se realizó un estudio descriptivo de caso con análisis documental y estático del software. La unidad de análisis es el proyecto ChemLab presente en el directorio de trabajo. El procedimiento describe la implementación observable, no reconstruye decisiones históricas no documentadas.

### 3.2 Fuentes y procedimiento

El análisis se efectuó en los siguientes pasos:

1. **Revisión de la estructura:** identificación de archivos Maven, código de producción, pruebas, recursos y documentos.
2. **Lectura del universo químico:** inspección de sustancias, reacciones, cantidades, inventarios, equipos, condiciones y resultados.
3. **Seguimiento de prácticas declarativas:** revisión del manifiesto, los 25 archivos de prácticas, el cargador JSON y sus validaciones.
4. **Seguimiento de reglas:** revisión de compilación de gramáticas, transiciones de autómatas, ejecución de acciones y cálculo de resultados.
5. **Seguimiento de funcionalidades de juego:** análisis de perfiles, persistencia, misiones, modos, feedback, resúmenes y avisos.
6. **Verificación cuantitativa:** recuento de entradas del catálogo y ejecución de `mvn test`.
7. **Síntesis:** organización de hallazgos desde los datos químicos hasta la experiencia visible.

La secuencia de construcción descrita en resultados es un orden conceptual de dependencias, no una cronología confirmada.

### 3.3 Evidencia y límites metodológicos

El recuento del proyecto encontró 114 archivos Java bajo `src/main/java` y 52 archivos Java bajo `src/test/java`. El catálogo contiene 36 sustancias y 25 reacciones, y el directorio de prácticas contiene 25 archivos JSON. En la ejecución de `mvn test` del 7 de noviembre de 2026, Maven reportó 261 pruebas, cero fallos, cero errores y cero omitidas.

La evidencia corresponde a los casos automatizados del repositorio. No equivale a cobertura total, prueba de usabilidad, validación de todas las sustancias contra fuentes experimentales ni garantía de que la aplicación se inicie correctamente en cualquier sistema. No se ejecutó una evaluación visual de la interfaz en una ventana de escritorio.

## 4. Resultados

### 4.1 Universo químico representado

El manifiesto del catálogo registra 36 sustancias y 25 reacciones (Proyecto ChemLab, s. f.-a). Entre las sustancias hay elementos como hidrógeno, oxígeno, carbono, hierro, sodio, cloro, azufre, cobre, calcio, silicio, magnesio, aluminio, fósforo y níquel; además de compuestos y materiales como agua, dióxido de carbono, amoníaco, cloruro de sodio, ácido clorhídrico, óxidos, diamante, grafito, solución salina y productos de corrosión. Las reacciones incluyen síntesis, combustión, oxidación, corrosión, precipitación, calcinación, disolución, separación por evaporación y rutas de producción de diamante.

El catálogo principal contiene las sustancias, las reacciones y el manifiesto de rutas a los archivos de prácticas. Cada archivo de práctica contiene su `idReaccion` y su definición declarativa. El cargador relaciona esas definiciones con las reacciones y sustancias del catálogo para construir los objetos del dominio.

La aplicación modela las sustancias como datos y las reacciones como relaciones entre reactivos y productos con coeficientes. Las condiciones se expresan mediante requisitos asociados a las reacciones. El equipo y las acciones se representan con objetos y enumeraciones del dominio. El catálogo no debe interpretarse como una base exhaustiva de sustancias, reacciones o equipos posibles.

El análisis de fórmulas dispone de una tabla periódica propia para validar símbolos y componer los resultados del parser. La nomenclatura tiene un alcance limitado y no debe presentarse como nomenclador químico general.

**[Insertar Figura 3 aquí: esquema de entidades `Sustancia`–`Reaccion`–`Inventario`–`Experimento`, vinculado al manifiesto y a los archivos de práctica.]**

### 4.2 Construcción lógica del proyecto, paso a paso

La siguiente secuencia explica cómo las partes del programa se conectan. Se presenta como orden conceptual de dependencias, no como cronología confirmada.

#### Paso 1. Definir las entidades del dominio

El universo químico se representa mediante objetos como `Sustancia`, `Reaccion`, `Experimento`, `Inventario`, `Equipo`, `CondicionesExperimentales`, `Objetivo` y `ResultadoQuimico`. Las enumeraciones acotan categorías como estado de materia, tipo de equipo, tipo de sustancia y acciones de laboratorio.

La reacción relaciona reactivos y productos; el experimento concentra el inventario, el equipo, las condiciones y el estado de ejecución. Esta modelación permite que el motor y la UI compartan conceptos coherentes sin representar cada valor como una cadena aislada.

#### Paso 2. Cargar el catálogo y las prácticas

`catalogo-quimico.json` registra sustancias, reacciones y las rutas de práctica. Cada una de las 25 reacciones tiene un archivo en `src/main/resources/practicas/`. `RepositorioCatalogoQuimicoJson` carga las definiciones externas y las enlaza con las entidades del dominio. El cargador exige una práctica externa para cada reacción y rechaza la antigua forma de práctica embebida en el catálogo.

Una práctica puede declarar objetivo, materiales iniciales, equipo, condiciones, etapas educativas, fases visibles de reactivos y gramática. El cargador valida la estructura y referencias requeridas; una definición inválida produce un error de carga en vez de quedar disponible parcialmente.

#### Paso 3. Crear el experimento desde los datos

`FabricaExperimento` construye el experimento a partir de la definición declarativa: resuelve materiales y producto contra el catálogo, crea el equipo y objetivo y carga las condiciones iniciales. También proporciona la variante con inventario vacío usada por el modo Difícil.

La fábrica no conserva una tabla de experimentos específica para cada ID de reacción. Las diferencias de práctica se declaran en los JSON; las operaciones y reglas comunes permanecen en el código.

#### Paso 4. Compilar la gramática a un autómata

La gramática de cada práctica declara el estado inicial, las producciones de transición y el estado final. `FabricaAutomata.crearAutomataParaReaccion` obtiene esa gramática desde la `Reaccion` y la pasa a `CompiladorGramaticaRegular`. El compilador produce los estados y transiciones del autómata que controla la secuencia permitida.

Se conservan autómatas genéricos estándar y simple para escenarios que no están asociados a una práctica declarativa. Ya no se utilizan `GramaticasProtocolos` ni configuraciones específicas por reacción en las fábricas para definir protocolos o experimentos.

#### Paso 5. Evaluar la química y aplicar el resultado

El motor químico coordina la evaluación estequiométrica, las reglas de reacción, el cálculo de productos y la actualización del inventario. El autómata comprueba el orden del procedimiento; el motor evalúa si la transformación química modelada puede ocurrir y qué resultado produce. Son responsabilidades distintas.

#### Paso 6. Coordinar la práctica en una sesión

`SesionJuego` coordina el experimento en curso, la reacción, el autómata, el modo de juego, las acciones registradas, las pistas, los productos y el historial. Notifica a los observadores cuando la práctica inicia, progresa, se completa, falla o se abandona.

Durante una operación, la sesión entrega la acción al flujo de validación y ejecución. Al completarse la práctica, registra el resultado y los productos en el cuaderno y actualiza el progreso del perfil cuando corresponda.

#### Paso 7. Presentar laboratorio, modos y perfiles

`Main` inicia JavaFX y presenta primero `SelectorPerfil`. Luego muestra la intro animada, que no avanza por temporizador: el usuario la cierra con **Espacio** o **Enter**. Desde el laboratorio se puede regresar al selector y cambiar de perfil.

El **Modo Tutorial** permite seleccionar cualquier práctica disponible y mantiene la guía de los protocolos. El **Modo Difícil** permite experimentar con inventario inicial vacío y recibir pistas; la ruta de misiones es opcional y solo aparece en este modo. El progreso de misiones no restringe las prácticas del Modo Tutorial.

Los perfiles persisten en archivos JSON locales por UUID bajo `.chemlab/profiles` en la carpeta personal del usuario. Se guardan misiones, descubrimientos, procesos completados e historial del cuaderno. El guardado es independiente por perfil y usa escritura atómica. Se reanuda el progreso entre prácticas, no un experimento que estuviera activo. Al cambiar de perfil con una práctica activa, se solicita confirmación y, si se acepta, se registra el abandono antes de guardar.

#### Paso 8. Ejecutar acciones en los puestos de trabajo

`VistaJuego2D` coordina mapa, movimiento, HUD, menús y entrada del jugador. `RenderizadorLaboratorio2D` dibuja el mapa, el personaje y los elementos visibles. `VistaPuestoLaboratorio` presenta el equipo y las herramientas, y permite cargar reactivos o accionar controles de la estación.

`PlanificadorEstacionesProcedimiento` determina el equipo/puesto requerido para determinados pasos. Esta planificación, junto con ciertos metadatos de rutas alternativas de síntesis, permanece en Java. Por tanto, las prácticas y sus gramáticas son declarativas, pero no toda decisión de navegación o presentación se configura actualmente desde JSON.

#### Paso 9. Mostrar retroalimentación, seguridad educativa y análisis

Durante las operaciones, el sistema muestra el mensaje del motor y la etapa actual. En Tutorial se ofrecen explicación de manejo y siguiente paso; en Difícil se invita a revisar materiales, condiciones y compatibilidad del equipo. La consola presenta lecturas de temperatura, presión, pH y atmósfera, los objetivos de paso cuando corresponda, el fenómeno modelado y las fases de reactivos/productos previstas.

El monitor genera alertas educativas para condiciones simuladas como temperatura o presión elevadas, atmósferas de oxígeno o cloro y solicitudes de ignición en esas atmósferas. Las alertas no bloquean acciones ni representan límites seguros reales.

Al completar o fallar una práctica, se muestra un resumen con el objetivo, la ecuación disponible, los productos registrados, una explicación de aprendizaje o aspectos por revisar y la secuencia de operaciones. El cuaderno permite consultar protocolos, analizar fórmulas y comparar rutas dentro del alcance implementado.

#### Paso 10. Verificar reglas con pruebas automatizadas

El proyecto incluye pruebas de entidades, cantidades, catálogo, prácticas externas, cálculo químico, estados y transiciones de autómatas, fábricas, sesiones, misiones, perfiles, procedimientos y componentes seleccionados de UI. La ejecución del 7 de noviembre de 2026 terminó con 261 pruebas exitosas, sin fallos, errores ni omisiones.

**[Mostrar, sin copiar el código en este informe, un caso representativo de `CalculadorEstequiometricoTest`, `FabricaAutomataTest`, `ProgresoPerfilTest` y `ExperimentosCatalogoIntegracionTest`. Usar los nombres actuales de los métodos de prueba.]**

### 4.3 Arquitectura y flujo de ejecución

| Componente | Responsabilidad principal |
|---|---|
| `model` | Entidades, valores, estados y excepciones del dominio químico. |
| `catalogo` | Consulta y organización del catálogo químico. |
| `repository` | Carga de catálogo/prácticas y persistencia local de perfiles. |
| `resources/practicas` | Definiciones JSON de las 25 prácticas. |
| `grammar` | Compilación de gramáticas de práctica y análisis de fórmulas. |
| `automata` | Estados, transiciones y validación del orden de acciones. |
| `engine` | Evaluación de condiciones, estequiometría, reacciones y productos. |
| `factory` | Construcción de experimentos y autómatas genéricos o compilados. |
| `game` | Sesión, perfiles, progreso, misiones, mapa y estaciones. |
| `service` | Conversión química y de unidades. |
| `ui` | Pantallas JavaFX, selector, intro, laboratorio, cuaderno y puestos. |

El flujo general es: **catálogo y práctica JSON → creación del experimento y compilación de gramática → sesión → acción del jugador → validación del autómata → evaluación del motor químico → actualización del inventario/resultado → notificación a la UI → registro en cuaderno y perfil**.

**[Insertar Figura 4 aquí: diagrama de secuencia desde seleccionar una práctica hasta registrar el producto. Mostrar por separado la decisión del autómata y la evaluación química.]**

### 4.4 Funcionalidades implementadas y pendientes

| Funcionalidad | Estado y alcance |
|---|---|
| Intro manual | Implementada; se cierra con Espacio o Enter, no por tiempo. |
| Retroalimentación de práctica | Implementada; contextual a Tutorial y Difícil. |
| Resumen de práctica | Implementado al completar o fallar, basado en el resultado registrado. |
| Misiones | Ruta opcional de cinco etapas, disponible únicamente en Modo Difícil. |
| Independencia del Tutorial | Implementada; todas las prácticas disponibles sin depender de misiones. |
| Perfiles | Implementados con guardado local JSON; se puede cambiar de perfil desde el juego. |
| Simulación observable | Implementadas lecturas, fases previstas, fenómeno modelado y alertas educativas no bloqueantes. |
| Prácticas declarativas | Las 25 reacciones tienen archivo independiente; las gramáticas se compilan a autómatas. |
| Accesibilidad extendida | Pendiente; faltan alternativas de teclado para arrastre, ajustes de texto/audio y reducción de movimiento. |

### 4.5 Tecnologías y configuración

El archivo `pom.xml` declara Java 25, JavaFX 25.0.5, Maven Compiler Plugin 3.14.0, JavaFX Maven Plugin 0.0.8, JUnit Jupiter 5.12.2, Jackson Databind 2.22.2 y Surefire 3.5.3 (Apache Maven, s. f.; JUnit Team, s. f.; OpenJFX, s. f.; Oracle, 2025). La aplicación es un proyecto Maven de un solo módulo. El README recomienda JDK 25 y Maven 3.9 o posterior. La ejecución de pruebas documentada en este informe se realizó con Maven 3.8.8; por ello se registra como entorno observado, no como modificación del requisito recomendado.

### 4.6 Límites de la migración a JSON

Las gramáticas de agua y CVD que antes estaban centralizadas en `GramaticasProtocolos.java` ahora se declaran en sus archivos de práctica; esa clase fue retirada. También se eliminaron de las fábricas las tablas de configuración de experimentos y autómatas por ID de reacción. `FabricaExperimento` consume las definiciones declarativas y `FabricaAutomata` compila la gramática asociada a cada reacción.

El catálogo principal contiene las sustancias, las reacciones y el manifiesto de prácticas externas; ya no concentra las definiciones completas de prácticas. La migración de las 25 prácticas no elimina el código común: el cargador, el compilador de gramáticas, los autómatas, el motor químico y la sesión siguen siendo necesarios para interpretar y ejecutar los datos.

La migración no convirtió en JSON todos los aspectos de la aplicación. La asignación de puestos y parte de los metadatos de rutas alternativas continúan configurados en Java. Asimismo, la orientación del jugador forma parte del Modo Tutorial; no existe un recorrido independiente de primer uso.

## 5. Discusión

### 5.1 Aportes observables

El aporte arquitectónico principal es mantener responsabilidades distintas para la interfaz, el proceso de juego y la química. La interfaz presenta estados y controles; la sesión coordina la práctica; el autómata valida el orden del procedimiento; el motor evalúa reactivos, condiciones y productos.

La migración de prácticas a JSON permite mantener por separado los datos de cada reacción sin duplicar la definición de protocolos en las fábricas. El papel de gramáticas y autómatas se conserva: los datos declaran secuencias y etapas, el compilador produce autómatas y el juego usa esos autómatas para validar las acciones. El enfoque evita tanto un catálogo central monolítico de prácticas como lógica específica por reacción dispersa entre fábricas.

Los perfiles y las misiones añaden persistencia y progresión sin introducir una base de datos. El resumen de práctica, la retroalimentación y la consola observable aportan información contextual durante el juego. Estas características son observables en la implementación, pero su impacto educativo todavía no se ha medido.

### 5.2 Límites y aspectos que requieren atención

1. **Alcance del catálogo.** Las 36 sustancias y 25 reacciones constituyen una selección; no representan toda la química.
2. **Alcance del modelo científico.** El sistema calcula reglas expresamente codificadas. La existencia de temperaturas, presiones o coeficientes en el modelo no demuestra por sí sola que todas las condiciones sean suficientes para describir experimentalmente una reacción real.
3. **Revisión de contenido JSON.** La validación del cargador comprueba estructura, referencias y gramáticas, pero no certifica por sí sola el balance químico, la validez experimental ni la seguridad.
4. **Configuración aún en Java.** La planificación de estaciones y ciertos metadatos de rutas alternativas no se han migrado a datos declarativos.
5. **Persistencia local.** Los perfiles se guardan en archivos de la máquina del usuario y no sincronizan entre equipos. No se restaura un experimento interrumpido en curso.
6. **Evaluación educativa pendiente.** Las pruebas de software verifican comportamientos programados, no si estudiantes retienen conceptos o mejoran su desempeño.
7. **Accesibilidad pendiente.** Las mejoras propuestas para controles alternativos, tamaño de texto, audio y reducción de movimiento aún no están implementadas.
8. **Validación visual pendiente.** La ejecución automatizada no sustituye una sesión de observación de la interfaz en sistemas operativos y tamaños de pantalla distintos.
9. **Seguridad real.** Las alertas son educativas, no bloqueantes y con umbrales propios del juego. No sustituyen protocolos institucionales, supervisión docente ni prácticas reales de seguridad química.

### 5.3 Mejoras recomendadas

- Implementar y probar las mejoras de accesibilidad y alternativas de control.
- Revisar químicamente las prácticas actuales y documentar fuentes para los datos de reacción, condiciones y nomenclatura.
- Decidir por separado si la planificación de estaciones y las rutas alternativas también deben convertirse en datos externos.
- Definir resultados de aprendizaje observables y evaluar la aplicación con usuarios mediante tareas guiadas y rúbricas.
- Añadir pruebas de integración de extremo a extremo para los flujos principales de interfaz, además de las pruebas automatizadas existentes.
- Mantener una única cifra vigente de pruebas en la documentación y evitar números rígidos cuando cambien con frecuencia.

## 6. Conclusiones

ChemLab integra un catálogo químico, prácticas declarativas, gramáticas regulares, autómatas finitos, un motor de evaluación y una interfaz de laboratorio 2D. Cada una de las 25 reacciones tiene una definición de práctica independiente en JSON. El sistema carga esos datos, compila las gramáticas a autómatas y valida con ellos el orden de las acciones; por separado, el motor químico calcula los resultados según las reglas implementadas.

La aplicación también dispone de perfiles con progreso local, misiones opcionales en Modo Difícil, un Tutorial independiente de la progresión, retroalimentación contextual, análisis final y avisos educativos de seguridad. La persistencia usa archivos JSON y no requiere una base de datos.

La ejecución de `mvn test` del 7 de noviembre de 2026 reportó 261 pruebas sin fallos, errores ni omisiones. Esto aporta evidencia positiva sobre los casos automatizados incluidos, pero no demuestra efectividad educativa, exactitud científica exhaustiva ni funcionamiento gráfico en todos los entornos. ChemLab constituye una base funcional de simulación química educativa; los pasos siguientes deberían incluir evaluación pedagógica, revisión científica de los datos, pruebas visuales y, si se decide ampliar la declaración de contenido, migrar a JSON la planificación de estaciones y las rutas alternativas pendientes.

## 7. Referencias bibliográficas

Apache Maven. (s. f.). *Maven documentation*. https://maven.apache.org/guides/

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.

Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2006). *Introduction to automata theory, languages, and computation* (3.ª ed.). Pearson.

International Union of Pure and Applied Chemistry. (s. f.). *Compendium of chemical terminology (the Gold Book)*. Recuperado el 7 de noviembre de 2026, de https://goldbook.iupac.org/

JUnit Team. (s. f.). *JUnit 5 user guide*. https://junit.org/junit5/docs/current/user-guide/

OpenJFX. (s. f.). *OpenJFX documentation*. Recuperado el 7 de noviembre de 2026, de https://openjfx.io/

OpenStax. (2019). *Chemistry 2e*. Rice University. https://openstax.org/details/books/chemistry-2e

Oracle. (2025). *Java Platform, Standard Edition 25 documentation*. https://docs.oracle.com/en/java/javase/25/

Proyecto ChemLab. (s. f.-a). *Catálogo químico y manifiesto de prácticas de ChemLab* [Archivos JSON].

Proyecto ChemLab. (s. f.-b). *Código fuente y pruebas automatizadas de ChemLab* [Software de computadora].

Proyecto ChemLab. (s. f.-c). *README del proyecto ChemLab* [Documento de trabajo].

Proyecto ChemLab. (s. f.-d). *Gramáticas y autómatas en ChemLab* [Código fuente y documentación].

Proyecto ChemLab. (s. f.-e). *Nuevas funcionalidades de ChemLab* [Documento de trabajo].

## Anexo A. Ubicación sugerida del código que se explicará en el informe

Las referencias siguientes apuntan a rutas vigentes. No incluyen números de línea para evitar que cambios posteriores de formato vuelvan obsoletas las referencias.

| Tema explicado | Archivo o carpeta |
|---|---|
| Punto de entrada, selección de perfil e intro | `src/main/java/com/chemlab/Main.java`; `src/main/java/com/chemlab/ui/SelectorPerfil.java`; `src/main/java/com/chemlab/ui/IntroChemLab.java` |
| Versiones y dependencias | `pom.xml` |
| Catálogo y manifiesto de prácticas | `src/main/resources/catalogo-quimico.json` |
| Definiciones externas | `src/main/resources/practicas/` (25 archivos JSON) |
| Lectura y validación del catálogo | `src/main/java/com/chemlab/repository/RepositorioCatalogoQuimicoJson.java` |
| Modelo de definición de práctica | `src/main/java/com/chemlab/model/entity/DefinicionPractica.java` |
| Construcción de experimentos | `src/main/java/com/chemlab/factory/FabricaExperimento.java` |
| Compilación de gramáticas y autómatas | `src/main/java/com/chemlab/grammar/CompiladorGramaticaRegular.java`; `src/main/java/com/chemlab/factory/FabricaAutomata.java`; `src/main/java/com/chemlab/automata/AutomataQuimico.java` |
| Reglas y cálculo químico | `src/main/java/com/chemlab/engine/MotorQuimico.java`; `src/main/java/com/chemlab/engine/CalculadorEstequiometrico.java` |
| Coordinación de sesión y misiones | `src/main/java/com/chemlab/game/SesionJuego.java`; `src/main/java/com/chemlab/game/GestorMisiones.java` |
| Progreso y persistencia de perfiles | `src/main/java/com/chemlab/game/PerfilJugador.java`; `src/main/java/com/chemlab/game/ProgresoPerfil.java`; `src/main/java/com/chemlab/repository/RepositorioPerfilesJson.java` |
| Retroalimentación de prácticas | `src/main/java/com/chemlab/ui/game/GeneradorRetroalimentacionPractica.java` |
| Planificación de estaciones | `src/main/java/com/chemlab/game/procedure/PlanificadorEstacionesProcedimiento.java` |
| Laboratorio y renderizado | `src/main/java/com/chemlab/ui/game/VistaJuego2D.java`; `src/main/java/com/chemlab/ui/game/RenderizadorLaboratorio2D.java` |
| Mesa de trabajo | `src/main/java/com/chemlab/ui/workbench/VistaPuestoLaboratorio.java` |
| Análisis de fórmulas | `src/main/java/com/chemlab/grammar/formula/AnalizadorFormulaQuimica.java` |
| Pruebas representativas | `src/test/java/com/chemlab/factory/FabricaAutomataTest.java`; `src/test/java/com/chemlab/game/ProgresoPerfilTest.java`; `src/test/java/com/chemlab/game/ExperimentosCatalogoIntegracionTest.java` |

## Anexo B. Ubicación sugerida de figuras

1. **Figura 1:** mapa conceptual del universo químico y su traducción a componentes de software.
2. **Figura 2:** arquitectura por paquetes y flujo de datos.
3. **Figura 3:** entidades químicas y relación entre catálogo, reacciones y prácticas declarativas.
4. **Figura 4:** diagrama de secuencia desde seleccionar una práctica hasta registrar el producto.
5. **Figura 5:** pantalla general del laboratorio 2D con mapa, personaje, estaciones y HUD.
6. **Figura 6:** mesa de trabajo con etapa, material, equipo y condiciones.
7. **Figura 7:** cuaderno químico en análisis de una muestra.

*Todas las figuras anteriores son espacios reservados; este archivo no contiene imágenes.*
