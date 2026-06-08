using System.Data;
using System.Text.Json;
using Hanger.Application.Abstractions;
using Hanger.Infrastructure.Data;
using Npgsql;
using NpgsqlTypes;

namespace Hanger.Infrastructure.Repositories;

public sealed class PostgresTableCrudRepository(NpgsqlDataSource dataSource, HangerSchema schema) : ITableCrudRepository
{
    public async Task<IReadOnlyList<IReadOnlyDictionary<string, object?>>> GetAllAsync(
        string tableName,
        CancellationToken cancellationToken)
    {
        var table = GetTable(tableName);
        var sql = $"SELECT * FROM {Quote(table.Name)} ORDER BY {DefaultOrderBy(table)}";

        await using var command = dataSource.CreateCommand(sql);
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);

        var records = new List<IReadOnlyDictionary<string, object?>>();
        while (await reader.ReadAsync(cancellationToken))
        {
            records.Add(ReadRow(reader));
        }

        return records;
    }

    public async Task<IReadOnlyDictionary<string, object?>?> GetByKeyAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        CancellationToken cancellationToken)
    {
        var table = GetTable(tableName);
        var sql = $"SELECT * FROM {Quote(table.Name)} WHERE {BuildWhere(table)}";

        await using var command = dataSource.CreateCommand(sql);
        AddKeyParameters(command, table, keyValues);

        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        return await reader.ReadAsync(cancellationToken) ? ReadRow(reader) : null;
    }

    public async Task<IReadOnlyDictionary<string, object?>> CreateAsync(
        string tableName,
        IReadOnlyDictionary<string, JsonElement> values,
        CancellationToken cancellationToken)
    {
        var table = GetTable(tableName);
        var valueLookup = ToCaseInsensitiveDictionary(values);
        var columns = table.Columns
            .Where(column => valueLookup.ContainsKey(column.Name) && table.InsertableColumns.Contains(column))
            .ToArray();

        ValidateColumns(table, valueLookup.Keys);

        if (columns.Length == 0)
        {
            throw new ArgumentException("Informe ao menos um campo para criar o registro.");
        }

        var columnList = string.Join(", ", columns.Select(column => Quote(column.Name)));
        var parameterList = string.Join(", ", columns.Select(column => ToSqlValueExpression(column, ParameterName(column.Name))));
        var sql = $"INSERT INTO {Quote(table.Name)} ({columnList}) VALUES ({parameterList}) RETURNING *";

        await using var command = dataSource.CreateCommand(sql);
        AddValueParameters(command, columns, valueLookup);

        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        if (!await reader.ReadAsync(cancellationToken))
        {
            throw new DataException("O banco de dados nao retornou o registro criado.");
        }

        return ReadRow(reader);
    }

    public async Task<IReadOnlyDictionary<string, object?>?> UpdateAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        IReadOnlyDictionary<string, JsonElement> values,
        CancellationToken cancellationToken)
    {
        var table = GetTable(tableName);
        var valueLookup = ToCaseInsensitiveDictionary(values);
        ValidateColumns(table, valueLookup.Keys);

        var columns = table.Columns
            .Where(column => valueLookup.ContainsKey(column.Name) && table.UpdatableColumns.Contains(column))
            .ToArray();

        if (columns.Length == 0)
        {
            throw new ArgumentException("Informe ao menos um campo editavel para atualizar o registro.");
        }

        var setList = string.Join(
            ", ",
            columns.Select(column => $"{Quote(column.Name)} = {ToSqlValueExpression(column, ParameterName(column.Name))}"));

        var sql = $"UPDATE {Quote(table.Name)} SET {setList} WHERE {BuildWhere(table)} RETURNING *";

        await using var command = dataSource.CreateCommand(sql);
        AddValueParameters(command, columns, valueLookup);
        AddKeyParameters(command, table, keyValues);

        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        return await reader.ReadAsync(cancellationToken) ? ReadRow(reader) : null;
    }

    public async Task<bool> DeleteAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        CancellationToken cancellationToken)
    {
        var table = GetTable(tableName);
        var sql = $"DELETE FROM {Quote(table.Name)} WHERE {BuildWhere(table)}";

        await using var command = dataSource.CreateCommand(sql);
        AddKeyParameters(command, table, keyValues);

        return await command.ExecuteNonQueryAsync(cancellationToken) > 0;
    }

    private TableDefinition GetTable(string tableName)
    {
        if (!schema.TryGetTable(tableName, out var table))
        {
            throw new ArgumentException($"Tabela '{tableName}' nao esta cadastrada para CRUD.");
        }

        return table;
    }

    private static string DefaultOrderBy(TableDefinition table)
    {
        var dateColumn = table.Columns.FirstOrDefault(column => column.Name is "created_at" or "updated_at");
        if (dateColumn is not null)
        {
            return $"{Quote(dateColumn.Name)} DESC";
        }

        return string.Join(", ", table.PrimaryKeys.Select(column => Quote(column.Name)));
    }

    private static string BuildWhere(TableDefinition table) =>
        string.Join(" AND ", table.PrimaryKeys.Select(column => $"{Quote(column.Name)} = @{KeyParameterName(column.Name)}"));

    private static void AddKeyParameters(
        NpgsqlCommand command,
        TableDefinition table,
        IReadOnlyDictionary<string, string> keyValues)
    {
        foreach (var keyColumn in table.PrimaryKeys)
        {
            if (!keyValues.TryGetValue(keyColumn.Name, out var rawValue) || string.IsNullOrWhiteSpace(rawValue))
            {
                throw new ArgumentException($"Informe a chave primaria '{keyColumn.Name}'.");
            }

            command.Parameters.AddWithValue(KeyParameterName(keyColumn.Name), ToClrValue(keyColumn, rawValue));
        }
    }

    private static void AddValueParameters(
        NpgsqlCommand command,
        IReadOnlyList<ColumnDefinition> columns,
        IReadOnlyDictionary<string, JsonElement> values)
    {
        foreach (var column in columns)
        {
            var parameter = command.Parameters.AddWithValue(ParameterName(column.Name), ToClrValue(column, values[column.Name]) ?? DBNull.Value);
            if (column.PostgreSqlEnumType is not null)
            {
                parameter.NpgsqlDbType = NpgsqlDbType.Text;
            }
        }
    }

    private static object? ToClrValue(ColumnDefinition column, JsonElement value)
    {
        if (value.ValueKind is JsonValueKind.Null or JsonValueKind.Undefined)
        {
            return null;
        }

        return column.Kind switch
        {
            ColumnKind.Guid => value.ValueKind == JsonValueKind.String && Guid.TryParse(value.GetString(), out var guid)
                ? guid
                : throw new ArgumentException($"Campo '{column.Name}' deve ser um UUID valido."),
            ColumnKind.Integer => value.ValueKind == JsonValueKind.Number && value.TryGetInt32(out var integer)
                ? integer
                : throw new ArgumentException($"Campo '{column.Name}' deve ser um inteiro."),
            ColumnKind.Double => value.ValueKind == JsonValueKind.Number && value.TryGetDouble(out var number)
                ? number
                : throw new ArgumentException($"Campo '{column.Name}' deve ser um numero."),
            ColumnKind.Boolean => value.ValueKind is JsonValueKind.True or JsonValueKind.False
                ? value.GetBoolean()
                : throw new ArgumentException($"Campo '{column.Name}' deve ser booleano."),
            ColumnKind.DateTime => value.ValueKind == JsonValueKind.String && value.TryGetDateTime(out var dateTime)
                ? dateTime
                : throw new ArgumentException($"Campo '{column.Name}' deve ser uma data ISO-8601."),
            ColumnKind.String => value.ValueKind == JsonValueKind.String
                ? value.GetString()
                : throw new ArgumentException($"Campo '{column.Name}' deve ser texto."),
            _ => throw new ArgumentOutOfRangeException(nameof(column))
        };
    }

    private static object ToClrValue(ColumnDefinition column, string value) =>
        column.Kind switch
        {
            ColumnKind.Guid => Guid.TryParse(value, out var guid)
                ? guid
                : throw new ArgumentException($"Chave '{column.Name}' deve ser um UUID valido."),
            ColumnKind.Integer => int.TryParse(value, out var integer)
                ? integer
                : throw new ArgumentException($"Chave '{column.Name}' deve ser um inteiro."),
            ColumnKind.Double => double.TryParse(value, out var number)
                ? number
                : throw new ArgumentException($"Chave '{column.Name}' deve ser um numero."),
            ColumnKind.Boolean => bool.TryParse(value, out var boolean)
                ? boolean
                : throw new ArgumentException($"Chave '{column.Name}' deve ser booleano."),
            ColumnKind.DateTime => DateTime.TryParse(value, out var dateTime)
                ? dateTime
                : throw new ArgumentException($"Chave '{column.Name}' deve ser uma data valida."),
            ColumnKind.String => value,
            _ => throw new ArgumentOutOfRangeException(nameof(column))
        };

    private static void ValidateColumns(TableDefinition table, IEnumerable<string> columnNames)
    {
        var invalidColumns = columnNames
            .Where(columnName => !table.ColumnsByName.ContainsKey(columnName))
            .ToArray();

        if (invalidColumns.Length > 0)
        {
            throw new ArgumentException($"Campos invalidos para '{table.Name}': {string.Join(", ", invalidColumns)}.");
        }
    }

    private static Dictionary<string, JsonElement> ToCaseInsensitiveDictionary(IReadOnlyDictionary<string, JsonElement> values) =>
        new(values, StringComparer.OrdinalIgnoreCase);

    private static IReadOnlyDictionary<string, object?> ReadRow(NpgsqlDataReader reader)
    {
        var values = new Dictionary<string, object?>(StringComparer.OrdinalIgnoreCase);

        for (var index = 0; index < reader.FieldCount; index++)
        {
            values[reader.GetName(index)] = reader.IsDBNull(index) ? null : reader.GetValue(index);
        }

        return values;
    }

    private static string ToSqlValueExpression(ColumnDefinition column, string parameterName)
    {
        var parameter = $"@{parameterName}";
        return column.PostgreSqlEnumType is null ? parameter : $"{parameter}::{column.PostgreSqlEnumType}";
    }

    private static string Quote(string identifier) => $"\"{identifier}\"";

    private static string ParameterName(string columnName) => $"p_{columnName}";

    private static string KeyParameterName(string columnName) => $"k_{columnName}";
}
