package com.github.bestheroz.standard.common.mybatis;

import com.github.bestheroz.standard.common.exception.BusinessException;
import com.github.bestheroz.standard.common.util.AuthenticationUtils;
import com.github.bestheroz.standard.common.util.CaseUtils;
import com.github.bestheroz.standard.common.util.MapperUtils;
import com.github.bestheroz.standard.common.util.NullUtils;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.lang.NonNull;

@Slf4j
public class SqlCommand {
  public static final String SELECT_ITEMS_BY_MAP_WITH_ORDER = "getItemsByMapWithOrder";
  public static final String SELECT_TARGET_ITEMS_BY_MAP_WITH_ORDER = "getTargetItemsByMapWithOrder";
  public static final String SELECT_ITEMS_BY_DATATABLE = "getItemsForDataTable";
  public static final String SELECT_ITEM_BY_MAP = "getItemByMap";
  public static final String COUNT_BY_DATATABLE = "countForDataTable";
  public static final String COUNT_BY_MAP = "countByMap";
  public static final String INSERT = "insert";
  public static final String INSERT_BATCH = "insertBatch";
  public static final String UPDATE_MAP_BY_MAP = "updateMapByMap";
  public static final String DELETE_BY_MAP = "deleteByMap";
  // 참고용: 각VO에 암호화 컬럼 정의 방법
  private static final String TABLE_COLUMN_NAME_CREATED_BY = "CREATED_BY";
  private static final String TABLE_COLUMN_NAME_CREATED = "CREATED";
  private static final String TABLE_COLUMN_NAME_UPDATED_BY = "UPDATED_BY";
  private static final String TABLE_COLUMN_NAME_UPDATED = "UPDATED";
  private static final String VARIABLE_NAME_CREATED_BY = "createdBy";
  private static final String VARIABLE_NAME_CREATED = "created";
  private static final String VARIABLE_NAME_UPDATED_BY = "updatedBy";
  private static final String VARIABLE_NAME_UPDATED = "updated";
  private static final String SYSDATE = "NOW()";
  public static final Set<String> EXCLUDE_FIELD_SET =
      Set.of("SERIAL_VERSION_U_I_D", "serialVersionUID", "E_N_C_R_Y_P_T_E_D__C_O_L_U_M_N__L_I_S_T");

  public String getTableName() {
    return getTableName(this.getEntityClass().getSimpleName());
  }

  public static String getTableName(final String javaClassName) {
    return StringUtils.lowerCase(CaseUtils.getCamelCaseToSnakeCase(javaClassName));
  }

  private void getSelectSql(final SQL sql, final Set<String> columns) {
    columns.forEach(
        column -> {
          final String dbColumnName = CaseUtils.getCamelCaseToSnakeCase(column);
          sql.SELECT(dbColumnName);
        });
  }

  private void verifyWhereKey(final Map<String, Object> whereConditions) {
    if (NullUtils.size(whereConditions) < 1) {
      log.warn("whereConditions is empty");
      throw BusinessException.FAIL_NO_DATA_SUCCESS;
    }
  }

  public String countByMap(final Map<String, Object> whereConditions) {
    final SQL sql = new SQL();
    sql.SELECT("COUNT(1) AS CNT").FROM(this.getTableName());
    this.getWhereSql(sql, whereConditions);
    log.debug(sql.toString());
    return sql.toString();
  }

  private Class<?> getEntityClass() {
    final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    final Optional<StackTraceElement> getItems =
        Arrays.stream(stackTrace)
            .filter(
                item -> {
                  try {
                    return (item.getClassName().startsWith("com.sun.proxy.$Proxy")
                        && List.of(
                                SELECT_ITEMS_BY_MAP_WITH_ORDER,
                                SELECT_TARGET_ITEMS_BY_MAP_WITH_ORDER,
                                SELECT_ITEMS_BY_DATATABLE,
                                SELECT_ITEM_BY_MAP,
                                COUNT_BY_MAP,
                                COUNT_BY_DATATABLE,
                                INSERT,
                                UPDATE_MAP_BY_MAP,
                                DELETE_BY_MAP)
                            .contains(item.getMethodName())
                        && Class.forName(item.getClassName()).getInterfaces().length > 0
                        && Class.forName(item.getClassName())
                                .getInterfaces()[0]
                                .getGenericInterfaces()
                                .length
                            > 0);
                  } catch (final ClassNotFoundException e) {
                    log.warn("Failed to getEntityClass");
                    throw BusinessException.ERROR_SYSTEM;
                  }
                })
            .findFirst();
    if (getItems.isEmpty()) {
      throw BusinessException.ERROR_SYSTEM;
    }
    final StackTraceElement item = getItems.get();
    try {
      final Class<?> cInterface = Class.forName(item.getClassName()).getInterfaces()[0];
      return Class.forName(
          StringUtils.substringBetween(
              cInterface.getGenericInterfaces()[0].getTypeName(), "<", ">"));
    } catch (final ClassNotFoundException e) {
      log.warn("Failed to getEntityClass");
      throw BusinessException.ERROR_SYSTEM;
    }
  }

  private int getRepositoryMethodParamLength() {
    final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    final Optional<StackTraceElement> getItems =
        Arrays.stream(stackTrace)
            .filter(
                item ->
                    (item.getClassName().startsWith("com.sun.proxy.$Proxy")
                        && List.of(
                                SELECT_ITEMS_BY_MAP_WITH_ORDER,
                                SELECT_TARGET_ITEMS_BY_MAP_WITH_ORDER,
                                SELECT_ITEMS_BY_DATATABLE,
                                SELECT_ITEM_BY_MAP,
                                COUNT_BY_MAP,
                                COUNT_BY_DATATABLE,
                                INSERT,
                                UPDATE_MAP_BY_MAP,
                                DELETE_BY_MAP)
                            .contains(item.getMethodName())))
            .findFirst();
    if (getItems.isEmpty()) {
      throw BusinessException.ERROR_SYSTEM;
    }
    final StackTraceElement item = getItems.get();
    try {
      return Arrays.stream(Class.forName(item.getClassName()).getInterfaces()[0].getMethods())
          .filter(method -> method.getName().equals(item.getMethodName()))
          .findFirst()
          .map(first -> first.getParameterTypes().length)
          .orElse(0);
    } catch (final ClassNotFoundException e) {
      log.warn("Failed to getRepositoryMethodParamLength");
      throw BusinessException.ERROR_SYSTEM;
    }
  }

  public String getItemsByMapWithOrder(
      final Map<String, Object> whereConditions, final List<String> orderByConditions) {
    return this.select(whereConditions, orderByConditions);
  }

  public String getTargetItemsByMapWithOrder(
      final Set<String> targetColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions) {
    return this.select(targetColumns, whereConditions, orderByConditions);
  }

  private String select(
      final Map<String, Object> whereConditions, final List<String> orderByConditions) {
    return this.select(this.getEntityFields(), whereConditions, orderByConditions);
  }

  // ordered list required
  private <T> Set<String> getEntityFields(final T entity) {
    return Stream.concat(
            Arrays.stream(entity.getClass().getDeclaredFields()).map(Field::getName),
            Arrays.stream(entity.getClass().getSuperclass().getDeclaredFields())
                .map(Field::getName))
        .distinct()
        .filter(fieldName -> !EXCLUDE_FIELD_SET.contains(fieldName))
        .collect(Collectors.toSet());
  }

  private Set<String> getEntityFields() {
    final Class<?> tClass = this.getEntityClass();
    return Stream.concat(
            Arrays.stream(tClass.getDeclaredFields()).map(Field::getName),
            Arrays.stream(tClass.getSuperclass().getDeclaredFields()).map(Field::getName))
        .distinct()
        .filter(fieldName -> !EXCLUDE_FIELD_SET.contains(fieldName))
        .collect(Collectors.toSet());
  }

  private String select(
      final Set<String> targetColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions) {
    final SQL sql = new SQL();
    this.getSelectSql(sql, targetColumns);
    sql.FROM(this.getTableName());
    this.getWhereSql(sql, whereConditions);
    orderByConditions.forEach(columns -> sql.ORDER_BY(CaseUtils.getCamelCaseToSnakeCase(columns)));
    log.debug(sql.toString());
    return sql.toString();
  }

  public String getItemByMap(@NonNull final Map<String, Object> whereConditions) {
    this.verifyWhereKey(whereConditions);
    return this.select(whereConditions, List.of());
  }

  public <T> String insert(@NonNull final T entity) {
    final Map<String, Object> param = MapperUtils.toMap(entity);
    final SQL sql = new SQL();
    sql.INSERT_INTO(getTableName(entity.getClass().getSimpleName()));
    param.entrySet().stream()
        .filter(
            item ->
                !StringUtils.equalsAny(
                    item.getKey(),
                    VARIABLE_NAME_CREATED,
                    VARIABLE_NAME_CREATED_BY,
                    VARIABLE_NAME_UPDATED,
                    VARIABLE_NAME_UPDATED_BY))
        .forEach(
            item ->
                sql.VALUES(
                    CaseUtils.getCamelCaseToSnakeCase(item.getKey()),
                    MessageFormat.format(
                        "#'{'{0}{1}'}'", item.getKey(), this.getJdbcType(item.getValue()))));

    final Set<String> fieldNames = this.getEntityFields(entity);

    if (fieldNames.contains(VARIABLE_NAME_CREATED)) {
      sql.VALUES(TABLE_COLUMN_NAME_CREATED, SYSDATE);
    }
    if (fieldNames.contains(VARIABLE_NAME_UPDATED)) {
      sql.VALUES(TABLE_COLUMN_NAME_UPDATED, SYSDATE);
    }
    if (fieldNames.contains(VARIABLE_NAME_CREATED_BY)) {
      sql.VALUES(TABLE_COLUMN_NAME_CREATED_BY, "'" + AuthenticationUtils.getId() + "'");
    }
    if (fieldNames.contains(VARIABLE_NAME_UPDATED_BY)) {
      sql.VALUES(TABLE_COLUMN_NAME_UPDATED_BY, "'" + AuthenticationUtils.getId() + "'");
    }

    log.debug(sql.toString());
    return sql.toString();
  }

  public <T> String insertBatch(@NonNull final List<T> entities) {
    if (NullUtils.isEmpty(entities)) {
      log.warn("entities empty");
      throw BusinessException.ERROR_SYSTEM;
    }
    final SQL sql = new SQL();
    sql.INSERT_INTO(getTableName(entities.get(0).getClass().getSimpleName()));
    final Set<String> columns = this.getEntityFields(entities.get(0));

    final String intoColumns =
        StringUtils.join(
            columns.stream()
                .map(CaseUtils::getCamelCaseToSnakeCase)
                .collect(Collectors.joining(",")));
    sql.INTO_COLUMNS(intoColumns);

    final List<List<Object>> valuesList = new ArrayList<>();
    entities.stream()
        .map(MapperUtils::toMap)
        .forEach(
            entity -> {
              final List<Object> values = new ArrayList<>();
              columns.forEach(
                  column -> {
                    if (StringUtils.equalsAny(
                        column, VARIABLE_NAME_CREATED, VARIABLE_NAME_UPDATED)) {
                      values.add(SYSDATE);
                    } else if (StringUtils.equalsAny(
                        column, VARIABLE_NAME_CREATED_BY, VARIABLE_NAME_UPDATED_BY)) {
                      values.add("'" + AuthenticationUtils.getId() + "'");
                    } else {
                      final Object o = entity.get(column);
                      final String value;
                      if (o == null) {
                        value = "null";
                      } else if (o instanceof String) {
                        value = MessageFormat.format("''{0}''", o);
                      } else if (o instanceof Set || o instanceof List) {
                        value = "'" + MapperUtils.toJsonArray(o).toString() + "'";
                      } else {
                        value = o.toString();
                      }
                      values.add(value);
                    }
                  });
              if (!values.contains(VARIABLE_NAME_CREATED)
                  && !values.contains(VARIABLE_NAME_UPDATED)
                  && !values.contains(VARIABLE_NAME_CREATED_BY)
                  && !values.contains(VARIABLE_NAME_UPDATED_BY)) {
                if (StringUtils.containsAny(
                    intoColumns, VARIABLE_NAME_CREATED, VARIABLE_NAME_UPDATED)) {
                  values.add(SYSDATE);
                } else if (StringUtils.containsAny(
                    intoColumns, VARIABLE_NAME_CREATED_BY, VARIABLE_NAME_UPDATED_BY)) {
                  values.add("'" + AuthenticationUtils.getId() + "'");
                }
              }
              valuesList.add(values);
            });
    sql.INTO_VALUES(
        valuesList.stream()
            .map(value -> StringUtils.join(value, ","))
            .collect(Collectors.joining("), (")));
    log.debug(sql.toString());
    return sql.toString();
  }

  public String updateMapByMap(
      final Map<String, Object> updateMap, final Map<String, Object> whereConditions) {
    this.verifyWhereKey(whereConditions);

    final SQL sql = new SQL();
    sql.UPDATE(this.getTableName());
    updateMap.forEach(
        (javaFieldName, value) -> {
          if (StringUtils.equalsAny(
              javaFieldName,
              VARIABLE_NAME_CREATED_BY,
              VARIABLE_NAME_CREATED,
              VARIABLE_NAME_UPDATED,
              VARIABLE_NAME_UPDATED_BY)) {
            return;
          }
          final String dbColumnName = CaseUtils.getCamelCaseToSnakeCase(javaFieldName);
          if (value instanceof Set) {
            final Set<?> value1 = (Set<?>) value;
            if (value1.isEmpty()) {
              sql.SET(MessageFormat.format("{0} = []", dbColumnName));
            } else {
              sql.SET(
                  MessageFormat.format(
                      "{0} = ''{1}''",
                      dbColumnName,
                      "["
                          + value1.stream()
                              .map(
                                  v -> {
                                    if (v instanceof String) {
                                      return MessageFormat.format("\"{0}\"", v);
                                    } else {
                                      return v.toString();
                                    }
                                  })
                              .collect(Collectors.joining(","))
                          + "]"));
            }
          } else if (value instanceof List) {
            final List<?> value1 = (List<?>) value;
            if (value1.isEmpty()) {
              sql.SET(MessageFormat.format("{0} = []", dbColumnName));
            } else {
              sql.SET(
                  MessageFormat.format(
                      "{0} = ''{1}''",
                      dbColumnName,
                      "["
                          + value1.stream()
                              .map(
                                  v -> {
                                    if (v instanceof String) {
                                      return MessageFormat.format("\"{0}\"", v);
                                    } else {
                                      return v.toString();
                                    }
                                  })
                              .collect(Collectors.joining(","))
                          + "]"));
            }
          } else {
            sql.SET(
                MessageFormat.format(
                    "{0} = #'{'param{3}.{1}{2}'}'",
                    dbColumnName, javaFieldName, this.getJdbcType(value), 1));
          }
        });

    whereConditions.forEach(
        (key, value) ->
            sql.WHERE(
                MessageFormat.format(
                    "{0} = #'{'whereConditions.{1}{2}'}'",
                    CaseUtils.getCamelCaseToSnakeCase(key), key, this.getJdbcType(value), 2)));

    this.getUpdateSetSql(sql, this.getEntityFields());
    if (!StringUtils.containsIgnoreCase(sql.toString(), "WHERE ")) {
      log.warn("whereConditions is empty");
      throw BusinessException.ERROR_SYSTEM;
    }
    // jdbcType=JSON 보정
    log.debug(sql.toString());
    return sql.toString();
  }

  private void getUpdateSetSql(final SQL sql, final Set<String> fieldNames) {
    if (fieldNames.contains(VARIABLE_NAME_UPDATED)) {
      sql.SET(TABLE_COLUMN_NAME_UPDATED + " = " + SYSDATE);
    }
    if (fieldNames.contains(VARIABLE_NAME_UPDATED_BY)) {
      sql.SET(
          MessageFormat.format(
              "{0} = ''{1}''", TABLE_COLUMN_NAME_UPDATED_BY, AuthenticationUtils.getId()));
    }
  }

  public String deleteByMap(final Map<String, Object> whereConditions) {
    this.verifyWhereKey(whereConditions);
    final SQL sql = new SQL();
    sql.DELETE_FROM(this.getTableName());
    this.getWhereSql(sql, whereConditions);
    this.requiredWhereConditions(sql);
    log.debug(sql.toString());
    return sql.toString();
  }

  public String countForDataTable(final DataTableFilterDTO dataTableFilterDTO) {
    if (StringUtils.isNotEmpty(dataTableFilterDTO.getSearch())) {
      for (final String searchColumn : dataTableFilterDTO.getSearchColumns()) {
        dataTableFilterDTO
            .getFilter()
            .put(
                MessageFormat.format("{0}:contains", searchColumn), dataTableFilterDTO.getSearch());
      }
    }

    final SQL sql = new SQL();
    sql.SELECT("COUNT(1) AS CNT").FROM(this.getTableName());
    Optional.ofNullable(dataTableFilterDTO.getFilter())
        .ifPresent(item -> this.getWhereBoundSql(sql, item));
    log.debug(sql.toString());
    return sql.toString();
  }

  public String getItemsForDataTable(final DataTableFilterDTO dataTableFilterDTO) {
    if (StringUtils.isNotEmpty(dataTableFilterDTO.getSearch())) {
      for (final String searchColumn : dataTableFilterDTO.getSearchColumns()) {
        dataTableFilterDTO
            .getFilter()
            .put(
                MessageFormat.format("{0}:contains", searchColumn), dataTableFilterDTO.getSearch());
      }
    }

    final SQL sql = new SQL();
    this.getSelectSql(sql, this.getEntityFields());
    sql.FROM(this.getTableName());

    Optional.ofNullable(dataTableFilterDTO.getFilter())
        .ifPresent(item -> this.getWhereBoundSql(sql, item));

    Optional.ofNullable(dataTableFilterDTO.getSortBy())
        .ifPresent(
            items ->
                items.forEach(
                    columns ->
                        sql.ORDER_BY(
                            columns.startsWith("-")
                                ? CaseUtils.getCamelCaseToSnakeCase(columns.replaceFirst("-", ""))
                                    + " DESC"
                                : CaseUtils.getCamelCaseToSnakeCase(columns) + " ASC")));
    if (dataTableFilterDTO.getPage() != 0) {
      sql.LIMIT(dataTableFilterDTO.getItemsPerPage());
      sql.OFFSET(dataTableFilterDTO.getStartIndex());
    }
    log.debug(sql.toString());
    return sql.toString();
  }

  private void requiredWhereConditions(final SQL sql) {
    if (!StringUtils.containsIgnoreCase(sql.toString(), "WHERE ")) {
      log.warn("whereConditions is empty");
      throw BusinessException.ERROR_SYSTEM;
    }
  }

  private String getJdbcType(final Object object) {
    final String jdbcType;
    if (object instanceof String || object instanceof Character) {
      jdbcType = ", jdbcType=VARCHAR";
    } else if (object instanceof Short) {
      jdbcType = ", jdbcType=SMALLINT";
    } else if (object instanceof Integer) {
      jdbcType = ", jdbcType=INTEGER";
    } else if (object instanceof Long) {
      jdbcType = ", jdbcType=BIGINT";
    } else if (object instanceof Double) {
      jdbcType = ", jdbcType=DOUBLE";
    } else if (object instanceof Instant) {
      jdbcType = ", jdbcType=TIMESTAMP";
    } else if (object instanceof Boolean) {
      jdbcType = ", jdbcType=BOOLEAN";
    } else if (object instanceof Byte[]) {
      jdbcType = ", jdbcType=BLOB";
    } else if (object instanceof Set || object instanceof List) {
      jdbcType = ", jdbcType=JSON";
    } else if (object == null) {
      jdbcType = ", jdbcType=NULL";
    } else {
      jdbcType = "";
      log.warn("케이스 빠짐 {}", object.getClass().getSimpleName());
    }
    return jdbcType;
  }

  private String getWhereString(final String conditionType) {
    final String whereString;
    switch (conditionType) {
      case "contains":
        whereString = "INSTR({0},  #'{'whereConditions.{1}{2}'}') > 0";
        break;
      case "notEqual":
      case "booleanNotEqual":
        whereString = "{0} <> #'{'whereConditions.{1}{2}'}'";
        break;
      case "notContains":
        whereString = "INSTR({0},  #'{'whereConditions.{1}{2}'}') = 0";
        break;
      case "startsWith":
        whereString = "INSTR({0},  #'{'whereConditions.{1}{2}'}') = 1";
        break;
      case "endsWith":
        whereString =
            "RIGHT({0}, CHAR_LENGTH(#'{'whereConditions.{1}{2}'}')) = #'{'whereConditions.{1}{2}'}'";
        break;
      case "lessThan":
        whereString = "{0} < #'{'whereConditions.{1}{2}'}'";
        break;
      case "lessThanOrEqual":
        whereString = "{0} <= #'{'whereConditions.{1}{2}'}'";
        break;
      case "greaterThan":
        whereString = "{0} > #'{'whereConditions.{1}{2}'}'";
        break;
      case "greaterThanOrEqual":
        whereString = "{0} >= #'{'whereConditions.{1}{2}'}'";
        break;
      case "in":
        whereString = "{0} IN ({1})";
        break;
      case "notIn":
        whereString = "{0} NOT IN ({1})";
        break;
      default:
        whereString = "{0} = #'{'whereConditions.{1}{2}'}'";
        break;
    }
    return whereString;
  }

  private void getWhereSql(final SQL sql, final Map<String, Object> whereConditions) {
    whereConditions.forEach(
        (key, value) -> {
          final String columnName = StringUtils.substringBefore(key, ":");
          final String conditionType =
              StringUtils.defaultString(StringUtils.substringAfter(key, ":"), "equals");
          if (StringUtils.equalsAny(conditionType, "in", "notIn")) {
            final Set<?> value1 = (Set<?>) value;
            if (!value1.isEmpty()) {
              final String str;
              final Object[] toArray = value1.toArray();
              if (toArray[0] instanceof String) {
                str =
                    StringUtils.defaultIfEmpty(
                        Arrays.stream(toArray)
                            .map(item -> "'" + item + "'")
                            .collect(Collectors.joining(",")),
                        "''");
              } else {
                str = StringUtils.defaultIfEmpty(StringUtils.join(toArray, ","), "");
              }
              if (StringUtils.equals(conditionType, "in")) {
                sql.WHERE(
                    MessageFormat.format(
                        "{0} IN ({1})", CaseUtils.getCamelCaseToSnakeCase(columnName), str));
              } else if (StringUtils.equals(conditionType, "notIn")) {
                sql.WHERE(
                    MessageFormat.format(
                        "{0} NOT IN ({1})", CaseUtils.getCamelCaseToSnakeCase(columnName), str));
              }
            }
          } else if (value instanceof String && value.equals("@NULL")) {
            sql.WHERE(
                MessageFormat.format("{0} is null", CaseUtils.getCamelCaseToSnakeCase(columnName)));
          } else if (value instanceof String && value.equals("@NOTNULL")) {
            sql.WHERE(
                MessageFormat.format(
                    "{0} is not null", CaseUtils.getCamelCaseToSnakeCase(columnName)));
          } else {
            final String whereString = this.getWhereString(conditionType);
            sql.WHERE(
                MessageFormat.format(
                    this.getRepositoryMethodParamLength() == 1
                        ? whereString.replace("whereConditions.", "")
                        : whereString,
                    CaseUtils.getCamelCaseToSnakeCase(columnName),
                    columnName,
                    this.getJdbcType(value)));
          }
        });
  }

  private void getWhereBoundSql(final SQL sql, final Map<String, Object> whereConditions) {
    whereConditions.forEach(
        (key, value) -> {
          final String columnName = StringUtils.substringBefore(key, ":");
          final String conditionType =
              StringUtils.defaultString(StringUtils.substringAfter(key, ":"), "equals");
          if (StringUtils.equalsAny(conditionType, "in", "notIn")) {
            final Set<?> value1 = (Set<?>) value;
            if (!value1.isEmpty()) {
              final String str;
              final Object[] toArray = value1.toArray();
              if (toArray[0] instanceof String) {
                str =
                    StringUtils.defaultIfEmpty(
                        Arrays.stream(toArray)
                            .map(item -> "'" + item + "'")
                            .collect(Collectors.joining(",")),
                        "''");
              } else {
                str = StringUtils.defaultIfEmpty(StringUtils.join(toArray, ","), "");
              }
              if (conditionType.equals("in")) {
                sql.WHERE(
                    MessageFormat.format(
                        "{0} IN ({1})", CaseUtils.getCamelCaseToSnakeCase(columnName), str));
              } else if (conditionType.equals("notIn")) {
                sql.WHERE(
                    MessageFormat.format(
                        "{0} NOT IN ({1})", CaseUtils.getCamelCaseToSnakeCase(columnName), str));
              }
            }
          } else if (value instanceof String && value.equals("@NULL")) {
            sql.WHERE(
                MessageFormat.format("{0} is null", CaseUtils.getCamelCaseToSnakeCase(columnName)));
          } else if (value instanceof String && value.equals("@NOTNULL")) {
            sql.WHERE(
                MessageFormat.format(
                    "{0} is not null", CaseUtils.getCamelCaseToSnakeCase(columnName)));
          } else {
            String whereString = this.getWhereString(conditionType);
            whereString = whereString.replace("whereConditions.", "");
            whereString = whereString.replace("{0}", CaseUtils.getCamelCaseToSnakeCase(columnName));
            if (value instanceof String
                && StringUtils.countMatches(((String) value), '-') == 2
                && StringUtils.countMatches(((String) value), ':') == 2
                && StringUtils.countMatches(((String) value), 'T') == 1
                && StringUtils.endsWith(((String) value), "Z")) {
              whereString =
                  whereString.replace(
                      "#'{'{1}{2}'}'",
                      "FROM_UNIXTIME("
                          + Integer.parseInt(
                              String.valueOf(Instant.parse((String) value).toEpochMilli() / 1000))
                          + ")");
            } else {
              if (value instanceof String) {
                whereString = whereString.replace("#'{'{1}{2}'}'", "'" + value + "'");
              } else {
                whereString = whereString.replace("#'{'{1}{2}'}'", value.toString());
              }
            }
            sql.WHERE(whereString);
          }
        });
  }
}
