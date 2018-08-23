# Report Engine

`$ git clone git@github.com:dvsmedeiros/report-engine.git`

`$ cd report-engine`

`$ ./mvnw clean install`

`$ java -Dreport.engine.home=<path_to_report-engine-0.0.1-SNAPSHOT.jar> -jar target/report-engine-0.0.1-SNAPSHOT.jar <report_id> <input.json>`

#### Configure path directories in ./config.properties

```
<report_id>  = id do relatório configurado em ./config/reports.json
<input.json> = nome do arquivo que contém o json do data source
```

```
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

#### Report Configuration ./config/config.properties/reports.json

```

[
  {
    "id" : 1,
    "description" : "Description Report",
    "name" : <jasper_file_name_without_extension>,
    "title" : "Sample",
    "version" : "1.0.0",
    "params" :
    [
      {
        "type" : "STRING",
        "name" : "title",
        "label" : "Title: ",
        "required" : true,
        "value" : "Title Sample Report"
      },
      ...
    ]
  }
]
```

#### Supported Parameters
```
STRING
{
    "type" : "STRING",
    "name" : "param_foo",
    "label" : "String Label: ",
    "required" : true,
    "value" : "bar"
}
INTEGER
{
    "type" : "INTEGER",
    "name" : "param_integer",
    "label" : "Integer Label: ",
    "required" : true,
    "value" : 1
}
LONG
{
    "type" : "LONG",
    "name" : "param_long",
    "label" : "Long Label: ",
    "required" : true,
    "value" : 1
}
DOUBLE
{
    "type" : "DOUBLE",
    "name" : "param_double",
    "label" : "Double Label",
    "required" : true,
    "value" : 1.0
}
DATE
{
    "type" : "DATE",
    "name" : "param_date",
    "label" : "Calendar Label: ",
    "required" : true,
    "value" : 1532923451130
}
BASE64
{
  "type" : "BASE64",
  "name" : "param_image",
  "label" : "Image: ",
  "required" : true,
  "value" : "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"
}
```
