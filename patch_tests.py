import os
import re

directory = r"c:\Users\klmxl\OneDrive\Escritorio\Documentos\ChemLab_EstructuraInicial (1)\ChemLab\src\test\java\com\chemlab\engine"

for filename in os.listdir(directory):
    if filename.endswith(".java"):
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Inject imports if needed
        if "CondicionesExperimentales" not in content and ("calculador.evaluar" in content or "calculador.calcular" in content):
            content = content.replace("import com.chemlab.model.entity.Inventario;", "import com.chemlab.model.entity.Inventario;\nimport com.chemlab.model.entity.CondicionesExperimentales;\nimport com.chemlab.model.enums.Atmosfera;")
        
        # Match calculador.evaluar(reaccion, inventario) or calculador.calcular(reaccion, inventario)
        # Note: can span multiple lines
        content = re.sub(r'calculador\.(evaluar|calcular)\(\s*([a-zA-Z0-9_]+(\([^)]*\))?|new [a-zA-Z0-9_]+\([^)]*\)|[a-zA-Z0-9_]+)\s*,\s*([a-zA-Z0-9_]+|new Inventario\(\))\s*\)',
                         r'calculador.\1(\2, \3, new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE))', content)
                         
        content = re.sub(r'calculador\.(evaluar|calcular)\(\s*([a-zA-Z0-9_]+(\([^)]*\))?|new [a-zA-Z0-9_]+\([^)]*\)|[a-zA-Z0-9_]+)\s*,\s*([a-zA-Z0-9_]+|new Inventario\(\))\s*,\s*([0-9\.]+)\s*\)',
                         r'calculador.\1(\2, \3, new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE), \4)', content)
                         
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
