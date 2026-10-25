# Gramáticas y autómatas en ChemLab

## Por qué se usan los dos

Una gramática describe cómo se pueden construir secuencias válidas. Un autómata consume esas acciones una por una y determina si la práctica puede avanzar desde su estado actual. En ChemLab, los símbolos terminales son acciones del laboratorio, como `CARGAR_REACTIVO`, `INICIAR_PLASMA` o `FINALIZAR`.

La gramática aporta una descripción declarativa del protocolo y permite enseñar sus derivaciones. El autómata conserva el papel que ya tenía en el juego: validar el orden de las acciones y mantener el estado de ejecución. El motor químico sigue comprobando reactivos, condiciones y productos mediante estequiometría. La gramática no reemplaza esa parte.

## Relación formal

La primera versión usa gramáticas regulares deterministas lineales por la derecha. Una producción `A → acción B` corresponde a una transición `A —acción→ B`. La producción `A → ε` marca un estado final. También se admiten producciones `A → acción`, que pasan a un estado final sintético.

Por ejemplo, el protocolo CVD se expresa así:

```text
PREPARACION → CARGAR_REACTIVO CARGADO
CARGADO → INICIAR_PLASMA PLASMA
PLASMA → CALENTAR DEPOSICION
DEPOSICION → FINALIZAR COMPLETADO
COMPLETADO → ε
```

Una derivación completa produce la palabra de acciones:

```text
CARGAR_REACTIVO · INICIAR_PLASMA · CALENTAR · FINALIZAR
```

El compilador convierte estas producciones a `EstadoQuimico` y `TransicionQuimica`. Por eso el protocolo sigue ejecutándose con el mismo autómata y las mismas acciones visibles en el puesto.

## Gramática libre de contexto para fórmulas

Los protocolos y las fórmulas son dos lenguajes distintos. Las acciones de un protocolo se describen con gramática regular y su autómata las valida durante el juego. Las fórmulas pueden anidar grupos, por ejemplo `Ca₃(PO₄)₂`; para reconocer esa recursión se usa una gramática libre de contexto y un parser descendente recursivo.

La gramática didáctica del analizador es:

```text
Fórmula → Secuencia
Secuencia → Término Secuencia | Término
Término → Elemento Subíndice? | ( Secuencia ) Subíndice?
Elemento → Mayúscula Minúscula?
Subíndice → Dígito+
```

El parser normaliza subíndices Unicode (`₂`) a dígitos (`2`), valida que el símbolo sea uno de los elementos químicos reconocidos, construye un árbol de derivación y calcula el conteo de átomos respetando los multiplicadores de grupo. Por ejemplo, `Ca₃(PO₄)₂` produce Ca: 3, P: 2 y O: 8.

Esta gramática libre de contexto no se compila al autómata de protocolos. El árbol de derivación lleva el seguimiento de grupos anidados, mientras que el autómata de acciones sigue validando las rutas de los experimentos.

## Integración implementada

- `com.chemlab.grammar` contiene los símbolos no terminales, las producciones, la gramática, el derivador, el compilador a autómata y el adaptador en dirección inversa.
- Las gramáticas fuente de síntesis de agua (`R_H2O`) y diamante CVD (`R_DIAMANTE_CVD`) se compilan al solicitar su autómata en `FabricaAutomata`.
- Los protocolos existentes que todavía están definidos directamente como autómatas pueden convertirse a una gramática regular equivalente para su consulta educativa.
- El historial del cuaderno ofrece las vistas **Autómata** y **Gramática y derivación**. La segunda muestra `N`, `Σ`, `P`, el símbolo inicial, las producciones y las reglas aplicadas a la ruta registrada.
- La pestaña **Gramáticas** del cuaderno tiene dos módulos: **Protocolos · regular** y **Fórmulas · libre de contexto**.
- El taller de protocolos permite practicar agua y CVD. El jugador elige producciones, construye una derivación y observa el no terminal y el estado del autómata en paralelo. Una producción aplicada desde el no terminal incorrecto se rechaza con una explicación.
- El analizador de fórmulas muestra la gramática, el árbol de derivación y el conteo de átomos. Incluye ejemplos como `Ca(OH)₂`, `Ca₃(PO₄)₂` y `Al₂(SO₄)₃`.
- Las rutas incompletas se muestran como prefijos válidos cuando sus acciones registradas se derivan correctamente; la última producción pendiente queda indicada.

## Alcance y límites de esta versión

El modelo exige que una acción desde un mismo no terminal determine una única producción. Esto corresponde al autómata determinista que ya usa el juego. `ε` se permite en un estado sin salidas, que representa el final del protocolo. Las condiciones como temperatura, presión, inventario y disponibilidad de materiales siguen en la lógica del experimento; no se codifican en la gramática de secuencias.

Los talleres son educativos y no guardan gramáticas nuevas. El analizador de fórmulas admite grupos con paréntesis, elementos y subíndices positivos; limita la entrada a 128 caracteres, 16 niveles de anidamiento y un millón de átomos por elemento. Aún no admite coeficientes estequiométricos externos, cargas iónicas, hidratos, puntos de adición ni anotaciones de fase como `(ac)` o nombres de alótropos. Esas expresiones requieren ampliar la gramática y definir cómo afectan el conteo.
