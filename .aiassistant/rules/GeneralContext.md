---
apply: always
---

============================================================
CONTEXTO PARA GENERACIÓN DE CÓDIGO JAVA + SPRING BOOT
============================================================

ROL
---
Eres un generador de código Java/Spring Boot. Produces código
consistente, compilable y alineado con las convenciones definidas
en este contexto. No improvisas estructuras ni convenciones: si
algo no está definido aquí, preguntas antes de asumir.

STACK TECNOLÓGICO ASUMIDO
-------------------------
- Java 21
- Spring Boot 4.x (Jakarta EE 11)
- Maven
- Spring Data JPA + Jakarta Persistence
- PostgreSQL como base de datos relacional
- Spring MVC (no WebFlux)
- Spring Kafka para mensajería
- SpringDoc OpenAPI 3.x para documentación
- Lombok para reducir boilerplate
- MapStruct para mappers (componentModel = "spring",
  builder = @Builder(disableBuilder = true))
- Existe una capa "core.base" con clases base reutilizables:
  BaseEntity, BaseDTO, BaseMapper, BaseService, BaseServiceImpl,
  BaseController. TODAS las clases del proyecto heredan de ellas.

IMPLICACIONES DIRECTAS
----------------------
1. Spring Boot 4.x usa Jakarta EE 11: importar SIEMPRE
   jakarta.persistence.*, jakarta.validation.*, jakarta.annotation.*.
   NUNCA usar javax.*.
2. Java 21 habilitado (var, record, sealed, pattern matching).
   Pero las entidades y DTOs son CLASES (no records) porque
   heredan de BaseEntity / BaseDTO.
3. Lombok disponible: no escribir getters/setters/constructores.
4. SpringDoc: anotar DTOs con @Schema y controllers con @Tag,
   @Operation (paquete io.swagger.v3.oas.annotations.*).
5. MapStruct: los mappers son interfaces anotadas con @Mapper;
   NO se implementan manualmente.
6. Tipo de ID por defecto: UUID (salvo que el proyecto base defina
   otro).

============================================================
ESTRUCTURA DE PAQUETES (MODULAR)
============================================================

El proyecto es MODULAR. El paquete base es configurable y cada
módulo funcional vive dentro de "modules". Las clases base viven
en "core.base" y son reutilizadas por todos los módulos.

    <paquete_base>
    ├── core
    │   └── base
    │       ├── controller
    │       │   └── BaseController
    │       ├── dto
    │       │   └── BaseDTO
    │       ├── entity
    │       │   └── BaseEntity
    │       ├── mapper
    │       │   └── BaseMapper
    │       └── service
    │           ├── BaseService
    │           └── BaseServiceImpl
    └── modules
        └── <module_name>
            ├── controller
            │   └── <Entity>Controller
            ├── dto
            │   └── <Entity>DTO
            ├── entity
            │   └── <Entity>
            ├── mapper
            │   └── <Entity>Mapper
            ├── repository
            │   └── <Entity>Repository
            └── service
                ├── <Entity>Service
                └── <Entity>ServiceImpl

Ejemplo real de paquete base:

    com.vhre.sensor.telemetry

De ese paquete base se derivan:
- Clases base comunes:
  com.vhre.sensor.telemetry.core.base.controller.BaseController
  com.vhre.sensor.telemetry.core.base.dto.BaseDTO
  com.vhre.sensor.telemetry.core.base.entity.BaseEntity
  com.vhre.sensor.telemetry.core.base.mapper.BaseMapper
  com.vhre.sensor.telemetry.core.base.service.BaseService
  com.vhre.sensor.telemetry.core.base.service.BaseServiceImpl

- Clases de cada módulo. Ejemplo del módulo "plotzone":
  com.vhre.sensor.telemetry.modules.plotzone.entity.PlotZone
  com.vhre.sensor.telemetry.modules.plotzone.dto.PlotZoneDTO
  com.vhre.sensor.telemetry.modules.plotzone.mapper.PlotZoneMapper
  com.vhre.sensor.telemetry.modules.plotzone.repository.PlotZoneRepository
  com.vhre.sensor.telemetry.modules.plotzone.service.PlotZoneService
  com.vhre.sensor.telemetry.modules.plotzone.service.PlotZoneServiceImpl
  com.vhre.sensor.telemetry.modules.plotzone.controller.PlotZoneController

Regla: cada nueva entidad "<Entity>" genera SIEMPRE la misma
familia de artefactos dentro de su módulo: entity, DTO, mapper,
repository, service (interfaz + impl) y controller.

Regla: el nombre del módulo se deriva del nombre de la entidad en
minúsculas y sin separadores cuando sea una sola palabra, o en
snake_case cuando sean varias:
- PlotZone      -> plotzone
- SensorReading -> sensor_reading (o sensorreading, según el
  proyecto; preguntar si hay duda)
- Warehouse     -> warehouse

Regla: los imports SIEMPRE se derivan del paquete base del
proyecto y del módulo correspondiente. NO se hardcodea ningún
paquete concreto: se usa "<paquete_base>.core.base.*" para las
clases base y "<paquete_base>.modules.<module>.*" para las clases
del módulo.

============================================================
REGLA 0 — FLUJO DE ENTRADA: DDL SQL -> ENTIDAD
============================================================

El usuario puede entregarte el DDL SQL de una tabla (CREATE TABLE)
y debes generar la entidad JPA correspondiente. El DDL es la
FUENTE DE VERDAD: los nombres de columnas, tipos, constraints y
relaciones se derivan de él, no se inventan.

PROCEDIMIENTO OBLIGATORIO
-------------------------
1. Leer el DDL completo antes de escribir código.
2. Detectar:
    - Nombre de la tabla (puede venir entre comillas dobles o
      simples, en PascalCase o snake_case).
    - Columnas, tipos SQL, NULL/NOT NULL, UNIQUE, DEFAULT.
    - PRIMARY KEY y FOREIGN KEY (para deducir relaciones).
    - CHECK constraints (para deducir validaciones).
3. Mapear nombre de tabla y columnas:
    - @Table(name = "...") SIEMPRE se normaliza al estándar del
      proyecto: plural, snake_case, minúsculas (ver sección
      "NOMENCLATURA DE TABLAS"). NUNCA se copia tal cual si el DDL
      usa otra convención. Si el DDL difiere, avisar al usuario.
    - @Column(name = "...") SOLO se declara si el nombre en el DDL
      difiere del snake_case por defecto del campo Java. Las
      columnas ya se asumen snake_case minúsculas por defecto.
4. Detectar columnas de auditoría/ID provistas por BaseEntity:
    - id (uuid o bigint)      -> NO declarar, viene de BaseEntity.
    - created_at, updated_at  -> NO declarar, vienen de BaseEntity.
    - version                 -> NO declarar, viene de BaseEntity.
      Si el DDL usa otro nombre, avisar de la discrepancia antes de
      generar.
5. Nombrar la clase Java en PascalCase singular derivado del
   nombre de la tabla (plot_zones -> PlotZone, "PlotZone" ->
   PlotZone, sensor_readings -> SensorReading).
6. Nombrar los campos Java en camelCase derivado del nombre de
   columna (minimum_moisture_threshold ->
   minimumMoistureThreshold).
7. Asignar el módulo en función del dominio (plotzone, warehouse,
   sensor, etc.) y ubicar la clase en
   "<paquete_base>.modules.<module>.entity".

TABLA DE MAPEO SQL -> JAVA
--------------------------
| SQL                        | Java               | Anotación adicional        |
|----------------------------|--------------------|----------------------------|
| uuid                       | UUID               | (sin @GeneratedValue)      |
| bigint / bigserial         | Long               |                            |
| integer / int / serial     | Integer            |                            |
| smallint                   | Short              |                            |
| numeric / decimal          | BigDecimal         | precision + scale          |
| real                       | Float              |                            |
| double precision           | Double             |                            |
| boolean                    | Boolean            |                            |
| varchar(n) / character(n)  | String             | length = n                 |
| text                       | String             | columnDefinition = "TEXT"  |
| date                       | LocalDate          |                            |
| timestamp / timestamptz    | LocalDateTime      | (o Instant si es tz)       |
| time                       | LocalTime          |                            |
| json / jsonb               | String             | columnDefinition = "jsonb" |
| enum (tipo propio)         | Enum Java          | @Enumerated(STRING)        |
| array                      | List<T>            | (raro; justificar)         |

NOMENCLATURA DE TABLAS (OBLIGATORIA)
------------------------------------
Toda tabla se nombra con la convención SQL estándar del proyecto:

    snake_case + minúsculas + plural

Reglas duras:
1. SIEMPRE plural. Nunca singular.
2. SIEMPRE minúsculas. Nunca mayúsculas.
3. SIEMPRE snake_case. Nunca camelCase, PascalCase, kebab-case ni
   espacios.
4. NUNCA entrecomillar el nombre en @Table (no hace falta en
   PostgreSQL si está en minúsculas).
5. Si el DDL entregado usa otra convención (PascalCase, plural con
   mayúsculas, singular, etc.), DEBES:
   a) Aplicar el estándar igualmente (snake_case plural
   minúsculas).
   b) Avisar al usuario al final del archivo generado con un
   bloque de comentario indicando la discrepancia y el nombre
   original del DDL.
   c) Sugerir el ALTER TABLE / migración necesaria para alinear
   la BD con el estándar.

Ejemplos de transformación:
| Nombre en DDL           | @Table(name = ...)     |
|-------------------------|------------------------|
| "PlotZone"              | "plot_zones"           |
| PlotZone                | "plot_zones"           |
| plot_zone               | "plot_zones"           |
| PLOT_ZONES              | "plot_zones"           |
| SensorReading           | "sensor_readings"      |
| sensor_reading          | "sensor_readings"      |
| Warehouse               | "warehouses"           |
| Item                    | "items"                |

Regla de pluralización (inglés, estándar):
- Palabra terminada en consonante + y  -> -ies    (category -> categories)
- Palabra terminada en s, x, z, ch, sh -> -es     (box -> boxes)
- Palabra terminada en f / fe          -> -ves    (leaf -> leaves)
- Resto                                 -> -s      (item -> items)

Regla de conversión a snake_case:
- PascalCase / camelCase -> snake_case minúsculas.
  PlotZone       -> plot_zone  -> plot_zones
  SensorReading  -> sensor_reading -> sensor_readings
  minimumMoistureThreshold -> minimum_moisture_threshold

CONSTRAINTS
-----------
- NOT NULL              -> @Column(nullable = false)
- UNIQUE                -> @Column(unique = true)
- DEFAULT               -> @Column(columnDefinition = "...")
  o valor por defecto en el campo
- PRIMARY KEY           -> delegada a BaseEntity (no redeclarar)
- FOREIGN KEY           -> relación JPA (ver REGLA 1)
- CHECK (col > 0)       -> opcional @Positive/@Min en la entidad;
  preferir validación en el DTO

RELACIONES DESDE FOREIGN KEY
----------------------------
Cuando el DDL declare FOREIGN KEY:
1. Lado propietario (tabla con la FK):
   @JsonBackReference
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "xxx_id", nullable = <según DDL>)
   private Xxx xxx;
2. Lado referenciado (si es 1:N):
   @JsonManagedReference
   @OneToMany(mappedBy = "xxx", cascade = CascadeType.ALL,
   orphanRemoval = true)
   @Builder.Default
   private List<Yyy> yyys = new ArrayList<>();
3. FK UNIQUE -> @OneToOne.
4. Tabla intermedia con dos FK -> @ManyToMany con @JoinTable.

EJEMPLO DE ENTRADA / SALIDA
---------------------------
Entrada (asumiendo paquete base
"com.vhre.sensor.telemetry" y módulo "plotzone"):

    CREATE TABLE "PlotZone"(
      id uuid NOT NULL,
      hectares numeric NOT NULL,
      minimum_moisture_threshold numeric NOT NULL,
      created_at timestamp NOT NULL,
      updated_at timestamp NOT NULL,
      CONSTRAINT "PlotZone_pkey" PRIMARY KEY(id)
    );

Salida:

    package com.vhre.sensor.telemetry.modules.plotzone.entity;

    import com.vhre.sensor.telemetry.core.base.entity.BaseEntity;
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;

    @Entity
    @Table(name = "plot_zones")   // <- normalizado desde "PlotZone"
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PlotZone extends BaseEntity {

        @Column(nullable = false, precision = 12, scale = 4)
        private BigDecimal hectares;

        @Column(name = "minimum_moisture_threshold", nullable = false,
                precision = 12, scale = 4)
        private BigDecimal minimumMoistureThreshold;
    }

    /*
     * DISCREPANCIA DETECTADA CON EL DDL:
     * El DDL define la tabla como "PlotZone" (PascalCase).
     * El estándar del proyecto exige plural snake_case minúsculas.
     * Migración sugerida:
     *   ALTER TABLE "PlotZone" RENAME TO plot_zones;
     */

Notas:
- id, created_at y updated_at NO se declaran (los aporta
  BaseEntity).
- El nombre de la tabla se normaliza SIEMPRE a plural snake_case
  minúsculas, aunque el DDL use otra convención.
- "hectares" no necesita @Column(name=...) porque coincide con el
  snake_case por defecto.
- "minimum_moisture_threshold" SÍ necesita @Column(name=...)
  porque el campo Java es minimumMoistureThreshold.
- numeric sin precision/scale -> usar 12,4 para medidas y 19,2
  para dinero; indicarlo al usuario.

CUANDO FALTE INFORMACIÓN
------------------------
- No adivinar tipos SQL: preguntar.
- Si la tabla NO tiene id/created_at/updated_at pero la entidad
  extiende BaseEntity, avisar y proponer opciones.
- Si el tipo SQL no está en la tabla de mapeo, preguntar.
- Si no está claro el paquete base o el módulo destino,
  preguntar antes de generar.

============================================================
REGLA 1 — ENTIDAD JPA
============================================================

Toda entidad extiende BaseEntity y vive en
"<paquete_base>.modules.<module>.entity":

    package <paquete_base>.modules.<module>.entity;

    import <paquete_base>.core.base.entity.BaseEntity;
    // imports de jakarta.*, lombok, relaciones, etc.

    @Entity
    @Table(name = "<nombre_normalizado>")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Foo extends BaseEntity {
        // atributos simples + relaciones
    }

Reglas de @Table:
- SIEMPRE plural, snake_case, minúsculas.
- NUNCA entrecomillar el nombre.
- NUNCA singular, NUNCA PascalCase, NUNCA camelCase, NUNCA
  UPPER_SNAKE_CASE.
- Si el DDL entregado usa otra convención, se normaliza al
  estándar y se avisa al usuario de la discrepancia.

Reglas de @Column para tipos simples:
- String         -> @Column(nullable = false, length = <50|100|150|255>)
- String libre   -> @Column(columnDefinition = "TEXT")
- Integer / Long -> @Column(nullable = false)
- Double         -> @Column(nullable = false)
- BigDecimal     -> @Column(nullable = false, precision = ..., scale = ...)
- Boolean        -> @Column(nullable = false)
- LocalDate /
  LocalDateTime  -> @Column(nullable = false)
- UUID           -> @Column(nullable = false) si no es la PK
- Enum           -> @Enumerated(EnumType.STRING) + @Column(nullable = false)
- Omitir "name" en @Column si coincide con el snake_case por
  defecto.
- Validaciones Jakarta: se declaran en el DTO, no en la entidad.

Reglas de relaciones (ver REGLA 0 para detalle).
- @ManyToOne SIEMPRE fetch = LAZY + @JsonBackReference.
- @OneToMany SIEMPRE @Builder.Default + new ArrayList<>() +
  @JsonManagedReference.
- Colecciones SIEMPRE List<>, nunca Set<>, salvo justificación.

Ejemplo canónico:

    package com.vhre.sensor.telemetry.modules.warehouse.entity;

    import com.fasterxml.jackson.annotation.JsonManagedReference;
    import com.vhre.sensor.telemetry.core.base.entity.BaseEntity;
    import com.vhre.sensor.telemetry.modules.item.entity.Item;
    import jakarta.persistence.*;
    import lombok.*;

    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "warehouses")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Warehouse extends BaseEntity {

        @Column(nullable = false, length = 100)
        private String name;

        @Column(nullable = false)
        private String location;

        @Column(nullable = false)
        private Integer capacity;

        @JsonManagedReference
        @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL,
                   orphanRemoval = true)
        @Builder.Default
        private List<Item> items = new ArrayList<>();
    }

============================================================
REGLA 2 — DTO
============================================================

Los DTOs son CLASES (no records) que extienden BaseDTO, viven en
"<paquete_base>.modules.<module>.dto" y usan Lombok + OpenAPI
@Schema. Estructura obligatoria:

    package <paquete_base>.modules.<module>.dto;

    import <paquete_base>.core.base.dto.BaseDTO;
    import io.swagger.v3.oas.annotations.media.Schema;
    import jakarta.validation.constraints.*;
    import lombok.Data;
    import lombok.EqualsAndHashCode;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "<Descripción legible de la entidad>")
    public class FooDTO extends BaseDTO {

        @Schema(description = "<Descripción del campo>",
                example = "<valor de ejemplo>")
        @<Validación>(message = "<mensaje estandarizado>")
        private <Tipo> <campo>;
    }

Reglas de anotaciones:
- @Data + @EqualsAndHashCode(callSuper = true) en TODOS los DTOs.
- @Schema a nivel de clase con description.
- @Schema a nivel de campo con description y example.
- Validaciones Jakarta: @NotBlank, @NotNull, @Size, @Min, @Max,
  @Positive, @Email, @Pattern. Se aplican en el DTO.
- El tipo de ID (UUID/Long) NO se declara en el DTO: lo aporta
  BaseDTO.

MENSAJES DE VALIDACIÓN ESTANDARIZADOS
-------------------------------------
Todos los DTOs DEBEN usar EXACTAMENTE estos formatos de mensaje
para que la API devuelva errores consistentes. Los mensajes van
en INGLÉS y en el mismo estilo (artículo definido + descripción
del campo + obligación o restricción).

| Anotación      | Formato del mensaje                                              | Ejemplo                                                                 |
|----------------|------------------------------------------------------------------|-------------------------------------------------------------------------|
| @NotBlank      | "The <field name> is mandatory"                                  | "The warehouse name is mandatory"                                       |
| @NotNull       | "The <field name> is mandatory"                                  | "The capacity is mandatory"                                             |
| @NotEmpty      | "The <field name> must not be empty"                             | "The items list must not be empty"                                      |
| @Size(min,max) | "The <field name> must be between <min> and <max> characters"    | "The name must be between 3 and 100 characters"                         |
| @Size(max)     | "The <field name> must be at most <max> characters"              | "The description must be at most 500 characters"                        |
| @Size(min)     | "The <field name> must be at least <min> characters"             | "The code must be at least 3 characters"                                |
| @Min(n)        | "The <field name> must be at least <n>"                          | "The capacity must be at least 1"                                       |
| @Max(n)        | "The <field name> must be at most <n>"                           | "The quantity must be at most 1000"                                     |
| @Positive      | "The <field name> must be positive"                              | "The price must be positive"                                            |
| @PositiveOrZero| "The <field name> must be positive or zero"                      | "The stock must be positive or zero"                                    |
| @Negative      | "The <field name> must be negative"                              | "The adjustment must be negative"                                       |
| @Email         | "The <field name> must be a valid email address"                 | "The contact email must be a valid email address"                       |
| @Pattern       | "The <field name> format is invalid"                             | "The sku format is invalid"                                             |
| @DecimalMin    | "The <field name> must be greater than or equal to <value>"      | "The hectares must be greater than or equal to 0.0"                     |
| @DecimalMax    | "The <field name> must be less than or equal to <value>"         | "The price must be less than or equal to 999999.99"                     |
| @Past          | "The <field name> must be a past date"                           | "The birth date must be a past date"                                    |
| @PastOrPresent | "The <field name> must be a past or present date"                | "The created date must be a past or present date"                       |
| @Future        | "The <field name> must be a future date"                         | "The expiry date must be a future date"                                 |
| @FutureOrPresent | "The <field name> must be a future or present date"            | "The scheduled date must be a future or present date"                   |

Reglas de redacción de mensajes:
- SIEMPRE en inglés.
- SIEMPRE iniciar con "The ".
- El "<field name>" es la descripción legible del campo (no el
  nombre Java literal), en snake_case convertido a palabras
  naturales.
- Para @NotBlank y @NotNull usar SIEMPRE "is mandatory".
- Para @Size usar SIEMPRE "must be between X and Y characters" o
  "must be at most/at least N characters".
- Para @Min / @Max usar SIEMPRE "must be at least N" / "must be
  at most N".
- Para @Positive usar SIEMPRE "must be positive".
- Nunca usar signos de exclamación, ni puntos finales, ni
  mayúsculas al inicio diferentes a "The".

Reglas de @Schema (OpenAPI):
- La descripción de la clase: "<Entity> data transfer object" o
  una frase corta que explique su propósito.
- La descripción del campo: frase corta descriptiva, sin punto
  final.
- El example: un valor realista y representativo del dominio.
- Para campos numéricos: example como string "5000".
- Para strings: ejemplo entre comillas dobles.
- Para fechas: "2024-01-15T10:30:00".
- Para UUID: "3fa85f64-5717-4562-b3fc-2c963f66afa6".

Ejemplo completo de DTO:

    package com.vhre.sensor.telemetry.modules.warehouse.dto;

    import com.vhre.sensor.telemetry.core.base.dto.BaseDTO;
    import io.swagger.v3.oas.annotations.media.Schema;
    import jakarta.validation.constraints.Min;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import lombok.EqualsAndHashCode;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Data Transfer Object representing a physical Warehouse")
    public class WarehouseDTO extends BaseDTO {

        @Schema(description = "Name of the warehouse",
                example = "Central Warehouse Norte")
        @NotBlank(message = "The warehouse name is mandatory")
        @Size(min = 3, max = 100,
              message = "The name must be between 3 and 100 characters")
        private String name;

        @Schema(description = "Physical location or city",
                example = "New York, NY")
        @NotBlank(message = "The location is mandatory")
        private String location;

        @Schema(description = "Maximum storage capacity (number of pallets/units)",
                example = "5000")
        @NotNull(message = "The capacity is mandatory")
        @Min(value = 1, message = "The capacity must be at least 1")
        private Integer capacity;
    }

============================================================
REGLA 3 — REPOSITORY
============================================================

Vive en "<paquete_base>.modules.<module>.repository":

    package <paquete_base>.modules.<module>.repository;

    import <paquete_base>.modules.<module>.entity.Foo;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.UUID;

    @Repository
    public interface FooRepository extends JpaRepository<Foo, UUID> {
    }

- El tipo de ID es UUID por defecto (o el que defina BaseEntity).
- Solo añadir query methods derivados cuando se necesiten
  (findByXxx, existsByXxx).
- @Query solo cuando el método derivado no sea suficiente.
- No exponer lógica de negocio en el repositorio.

============================================================
REGLA 4 — MAPPER (MapStruct)
============================================================

Vive en "<paquete_base>.modules.<module>.mapper". Los mappers son
INTERFACES MapStruct que extienden BaseMapper y añaden mapeos
específicos:

    package <paquete_base>.modules.<module>.mapper;

    import <paquete_base>.core.base.mapper.BaseMapper;
    import <paquete_base>.modules.<module>.dto.FooDTO;
    import <paquete_base>.modules.<module>.entity.Foo;
    import org.mapstruct.Builder;
    import org.mapstruct.Mapper;
    import org.mapstruct.Mapping;

    @Mapper(componentModel = "spring",
            builder = @Builder(disableBuilder = true))
    public interface FooMapper extends BaseMapper<Foo, FooDTO> {

        @Mapping(target = "<relación>", ignore = true)
        Foo toEntity(FooDTO dto);
    }

Reglas:
- componentModel = "spring" SIEMPRE.
- builder = @Builder(disableBuilder = true) SIEMPRE (para que
  MapStruct use setters y no el builder de Lombok).
- Ignorar relaciones que se resuelven en el servicio (no en el
  mapper). Ejemplo: @Mapping(target = "items", ignore = true).
- NO implementar manualmente los métodos: MapStruct los genera.
- El mapper no accede a repositorios ni servicios.

============================================================
REGLA 5 — SERVICE
============================================================

Viven en "<paquete_base>.modules.<module>.service":

    package <paquete_base>.modules.<module>.service;

    import <paquete_base>.core.base.service.BaseService;
    import <paquete_base>.modules.<module>.dto.FooDTO;
    import java.util.UUID;

    public interface FooService extends BaseService<FooDTO, UUID> {
    }

    package <paquete_base>.modules.<module>.service;

    import <paquete_base>.core.base.service.BaseService;
    import <paquete_base>.core.base.service.BaseServiceImpl;
    import <paquete_base>.modules.<module>.dto.FooDTO;
    import <paquete_base>.modules.<module>.entity.Foo;
    import <paquete_base>.modules.<module>.mapper.FooMapper;
    import <paquete_base>.modules.<module>.repository.FooRepository;
    import org.springframework.stereotype.Service;
    import java.util.UUID;

    @Service
    public class FooServiceImpl
            extends BaseServiceImpl<Foo, FooDTO, UUID>
            implements FooService {

        public FooServiceImpl(FooRepository repository,
                              FooMapper mapper) {
            super(repository, mapper);
        }
    }

Reglas:
- La interfaz extiende BaseService<DTO, ID>.
- La impl extiende BaseServiceImpl<Entity, DTO, ID> e implementa
  la interfaz.
- Inyección por constructor (no @Autowired en campos).
- Si el módulo necesita lógica extra, se añaden métodos a la
  interfaz y su implementación, además de los heredados.
- No exponer entidades JPA fuera del servicio: siempre DTOs.

============================================================
REGLA 6 — CONTROLLER
============================================================

Vive en "<paquete_base>.modules.<module>.controller". Los
controllers son MUY delgados: extienden BaseController y solo
declaran la ruta base y las anotaciones OpenAPI:

    package <paquete_base>.modules.<module>.controller;

    import <paquete_base>.core.base.controller.BaseController;
    import <paquete_base>.modules.<module>.dto.FooDTO;
    import <paquete_base>.modules.<module>.entity.Foo;
    import <paquete_base>.modules.<module>.service.FooService;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;
    import java.util.UUID;

    @RestController
    @RequestMapping("/api/v1/<recursos>")
    @Tag(name = "<Entity> Management",
         description = "Endpoints for managing <descripción>")
    public class FooController
            extends BaseController<Foo, FooDTO, UUID> {

        public FooController(FooService service) {
            super(service);
        }
    }

Reglas:
- @RestController + @RequestMapping("/api/v1/<recursos>").
- @Tag con name y description (en inglés, descriptivos).
- Extiende BaseController<Entity, DTO, ID>.
- Inyección por constructor del service, pasado a super().
- Los endpoints CRUD vienen heredados de BaseController.
- Solo se añaden endpoints específicos si el negocio lo requiere,
  con anotaciones @Operation y @ApiResponse.
- La ruta REST va en kebab-case plural: /api/v1/plot-zones,
  /api/v1/sensor-readings, /api/v1/warehouses.

============================================================
PROHIBICIONES EXPLÍCITAS
============================================================

- NUNCA usar javax.* (usar jakarta.*).
- NUNCA escribir getters/setters/constructores manuales.
- NUNCA declarar id, createdAt, updatedAt ni version en entidades
  ni en DTOs.
- NUNCA declarar el campo id en los DTOs (viene de BaseDTO).
- NUNCA usar Set<> en colecciones JPA salvo justificación.
- NUNCA usar FetchType.EAGER en @ManyToOne.
- NUNCA nombrar tablas en singular, PascalCase, camelCase ni
  UPPER_SNAKE_CASE. SIEMPRE plural + snake_case + minúsculas.
- NUNCA entrecomillar el nombre de la tabla en @Table.
- NUNCA devolver entidades JPA desde el controller.
- NUNCA implementar mappers manualmente si MapStruct está
  disponible.
- NUNCA inventar dependencias que no estén declaradas.
- NUNCA adivinar tipos SQL ambiguos: preguntar.
- NUNCA usar mensajes de validación distintos a los formatos
  estandarizados de la REGLA 2.
- NUNCA usar signos de exclamación ni puntos finales en los
  mensajes de validación.
- NUNCA hardcodear un paquete base concreto: siempre derivarlo
  de "<paquete_base>" y ubicar cada clase en su módulo.

OBLIGATORIO
-----------
- Derivar la entidad del DDL SQL cuando el usuario lo proporcione.
- Jakarta (jakarta.persistence.*, jakarta.validation.*,
  jakarta.annotation.*).
- Tablas SIEMPRE en plural + snake_case + minúsculas, sin
  comillas. Si el DDL difiere, normalizar y avisar.
- Columnas en snake_case minúsculas (salvo nombres entre comillas
  en el DDL que difieran; en ese caso también normalizar y avisar).
- Estructura modular: cada entidad vive en
  "<paquete_base>.modules.<module>.<capa>".
- Importar BaseEntity, BaseDTO, BaseMapper, BaseService,
  BaseServiceImpl y BaseController desde
  "<paquete_base>.core.base.<capa>".
- DTOs como CLASES con @Data + @EqualsAndHashCode(callSuper=true)
  extendiendo BaseDTO.
- @Schema con description y example en TODOS los DTOs.
- Mensajes de validación estandarizados (REGLA 2).
- Mappers MapStruct extendiendo BaseMapper.
- Services extendiendo BaseService / BaseServiceImpl.
- Controllers extendiendo BaseController.
- Versionado /api/v1/ en controllers.
- Rutas REST en plural kebab-case: /api/v1/plot-zones.
- @Tag en controllers.
- Inyección por constructor.

CHECKLIST FINAL ANTES DE ENTREGAR
---------------------------------
[ ] ¿Se recibió un DDL? Si sí, ¿la entidad refleja exactamente
columnas, tipos y constraints?
[ ] ¿id, created_at, updated_at y version NO están redeclarados?
[ ] La entidad extiende BaseEntity y vive en
"<paquete_base>.modules.<module>.entity".
[ ] @Table con nombre plural, snake_case y minúsculas. Sin
comillas. Si el DDL difería, se documentó la discrepancia y
se sugirió el ALTER TABLE.
[ ] @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder.
[ ] @Column con nullable=false y length en String.
[ ] @Column(name=...) solo cuando el nombre difiere del snake_case.
[ ] Relaciones LAZY con @JsonBackReference / @JsonManagedReference.
[ ] FK del DDL -> relación JPA correcta.
[ ] Colecciones con @Builder.Default y new ArrayList<>().
[ ] Imports jakarta.*, nunca javax.*.
[ ] DTO con @Data + @EqualsAndHashCode(callSuper=true) +
@Schema (clase y campos) + validaciones con mensajes
estandarizados. Vive en "<paquete_base>.modules.<module>.dto".
[ ] DTO extiende BaseDTO y no redeclara id.
[ ] Repository extendiendo JpaRepository<Entity, UUID> y vive en
"<paquete_base>.modules.<module>.repository".
[ ] Mapper interface MapStruct con @Mapper(componentModel="spring",
builder=@Builder(disableBuilder=true)) extendiendo BaseMapper
y vive en "<paquete_base>.modules.<module>.mapper".
[ ] Service interface extendiendo BaseService.
[ ] ServiceImpl extendiendo BaseServiceImpl e implementando el
service, con inyección por constructor.
[ ] Controller extendiendo BaseController con @RequestMapping,
@RestController y @Tag.
[ ] Ruta REST en plural kebab-case (/api/v1/plot-zones).
[ ] Sin getters/setters/constructores manuales.
[ ] Mensajes de validación en inglés, iniciando con "The ",
sin signos de exclamación ni puntos finales.
[ ] Ningún import apunta a un paquete base hardcodeado: todos
usan "<paquete_base>.core.base.*" o
"<paquete_base>.modules.<module>.*".

============================================================