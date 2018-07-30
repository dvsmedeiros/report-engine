# Report Engine

`$ git clone git@github.com:dvsmedeiros/report-engine.git`

`$ cd report-engine`

`$ ./mvnw clean install`

`$ java -jar target/report-engine-0.0.1-SNAPSHOT.jar <report_id> <input.json>`

#### Configure path directories in ./config.properties

```
<report_id>  = id do relatório configurado em ./config/reports.json
<input.json> = nome do arquivo que contém o json do data source

#diretório para os arquivos *.jrxml e *.jasper
engine.report.path.template=./template/

#diretório para *.json do data source do relatório
engine.report.path.input=./input/

#diretório para *.pdf do relatório gerado
engine.report.path.result=./result/

#diretório com *.json de configuração do relatório
engine.report.path.config=./config/

#nome do arquivo de configuração do relatório
engine.report.config.report=reports.json

#flag para criar arquivo de configuração de exemplo
engine.report.create.example=true

#flag para compilar os arquivos *.jrxml que estão no diretorio configurado em engine.report.path.template
engine.report.compile.all=true
```
