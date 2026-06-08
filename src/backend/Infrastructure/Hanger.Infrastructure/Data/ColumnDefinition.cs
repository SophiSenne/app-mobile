namespace Hanger.Infrastructure.Data;

public sealed record ColumnDefinition(
    string Name,
    ColumnKind Kind,
    bool IsPrimaryKey = false,
    bool IsDatabaseGenerated = false,
    string? PostgreSqlEnumType = null);

