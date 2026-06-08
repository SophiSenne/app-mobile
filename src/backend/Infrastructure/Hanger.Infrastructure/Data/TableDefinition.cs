namespace Hanger.Infrastructure.Data;

public sealed class TableDefinition
{
    public TableDefinition(string name, params ColumnDefinition[] columns)
    {
        Name = name;
        Columns = columns;
        ColumnsByName = columns.ToDictionary(column => column.Name, StringComparer.OrdinalIgnoreCase);
        PrimaryKeys = columns.Where(column => column.IsPrimaryKey).ToArray();
    }

    public string Name { get; }

    public IReadOnlyList<ColumnDefinition> Columns { get; }

    public IReadOnlyDictionary<string, ColumnDefinition> ColumnsByName { get; }

    public IReadOnlyList<ColumnDefinition> PrimaryKeys { get; }

    public IEnumerable<ColumnDefinition> InsertableColumns => Columns.Where(column => !column.IsDatabaseGenerated);

    public IEnumerable<ColumnDefinition> UpdatableColumns => Columns.Where(column => !column.IsPrimaryKey);
}

