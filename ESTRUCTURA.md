# Estructura de ChemLab

El proyecto mantiene el dominio químico separado de la simulación 2D y de JavaFX. Las gramáticas describen secuencias válidas para algunos protocolos y se compilan a autómatas; el autómata valida el orden de las acciones durante la ejecución, y el motor químico calcula el resultado.

## Paquetes principales

- `model/`: entidades, valores y enumeraciones del dominio.
- `automata/`: estados, transiciones y ejecución de las acciones químicas.
- `grammar/`: producciones regulares de protocolos, derivación de rutas y conversión entre gramáticas y autómatas.
- `grammar/formula/`: parser libre de contexto para fórmulas, con árbol de derivación y conteo de átomos.
- `factory/`: creación de experimentos, sesiones y autómatas.
- `repository/`: persistencia y carga del catálogo.
- `game/`: sesión y observadores del juego, sin dibujo.
- `game/world/`: jugador, mapa, colisiones y estaciones físicas.
- `game/procedure/`: protocolo de manejo y planificación de rutas entre puestos.
- `ui/game/`: pantalla jugable y renderer del mapa.
- `ui/grammar/`: taller de derivación que mantiene sincronizados el símbolo no terminal y el estado del autómata.
- `ui/workbench/`: estación de trabajo inmersiva y controles del procedimiento.
- `ui/workbench/VisualizadorReaccion`: metadatos visuales para colores, fenómenos y fases de productos, aislados de las reglas del autómata.
- `ui/components/`: componentes visuales reutilizables, como los recipientes de sustancias.

## Interacción en los puestos

Al acercarse al puesto correcto y pulsar **F**, se abre una vista de mesa completa con herramientas específicas para ese equipo. En los pasos de carga, los reactivos se arrastran desde la consola hasta el recipiente ilustrado. En los pasos operativos, se hace clic sobre el mando o la herramienta del aparato. El renderer anima calor, llama, enfriamiento, agitación, plasma, presión e ignición, mientras el autómata aplica el cambio químico real.

`PlanificadorEstacionesProcedimiento` decide cuándo el protocolo necesita otro equipo. La sesión cierra la mesa actual y guía al jugador al siguiente puesto. `VistaJuego2D` coordina la sesión y la entrada; `VistaPuestoLaboratorio` presenta la estación y recoge la interacción; `RenderizadorLaboratorio2D` dibuja el mapa.

## Controles

- **WASD** o flechas: mover al personaje.
- **F**: abrir el puesto cercano cuando corresponde al paso actual.
- Arrastrar un reactivo al aparato o accionar la herramienta resaltada.
- **E**: inventario y cuaderno.
- **M**: almacén y selección de práctica.
- **Esc**: cerrar el puesto o el menú actual.

La organización admite incorporar estaciones y herramientas nuevas sin trasladar reglas químicas al renderer. Los patrones existentes (Factory, Strategy, State, Observer y Repository) se conservan en sus capas actuales; cada paquete nuevo mantiene una responsabilidad concreta.
