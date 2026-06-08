namespace Hanger.Infrastructure.Data;

public sealed class HangerSchema
{
    private readonly Dictionary<string, TableDefinition> _tables;

    public HangerSchema()
    {
        var tables = new[]
        {
            new TableDefinition(
                "users",
                new("id", ColumnKind.Guid, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("username", ColumnKind.String),
                new("email", ColumnKind.String),
                new("password_hash", ColumnKind.String),
                new("bio", ColumnKind.String),
                new("avatar_url", ColumnKind.String),
                new("location_city", ColumnKind.String),
                new("created_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),

            new TableDefinition(
                "categories",
                new("id", ColumnKind.Integer, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("name", ColumnKind.String)),

            new TableDefinition(
                "types",
                new("id", ColumnKind.Integer, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("category_id", ColumnKind.Integer),
                new("name", ColumnKind.String)),

            new TableDefinition(
                "posts",
                new("id", ColumnKind.Guid, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("user_id", ColumnKind.Guid),
                new("image_url", ColumnKind.String),
                new("title", ColumnKind.String),
                new("caption", ColumnKind.String),
                new("weather_condition", ColumnKind.String),
                new("temperature", ColumnKind.Double),
                new("city", ColumnKind.String),
                new("share_count", ColumnKind.Integer, IsDatabaseGenerated: true),
                new("created_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),

            new TableDefinition(
                "post_tags",
                new("post_id", ColumnKind.Guid, IsPrimaryKey: true),
                new("category_id", ColumnKind.Integer, IsPrimaryKey: true),
                new("type_id", ColumnKind.Integer)),

            new TableDefinition(
                "follows",
                new("follower_id", ColumnKind.Guid, IsPrimaryKey: true),
                new("following_id", ColumnKind.Guid, IsPrimaryKey: true),
                new("created_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),

            new TableDefinition(
                "likes",
                new("user_id", ColumnKind.Guid, IsPrimaryKey: true),
                new("post_id", ColumnKind.Guid, IsPrimaryKey: true),
                new("created_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),

            new TableDefinition(
                "comments",
                new("id", ColumnKind.Guid, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("post_id", ColumnKind.Guid),
                new("user_id", ColumnKind.Guid),
                new("content", ColumnKind.String),
                new("created_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),

            new TableDefinition(
                "notifications",
                new("id", ColumnKind.Guid, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("recipient_id", ColumnKind.Guid),
                new("sender_id", ColumnKind.Guid),
                new("type", ColumnKind.String, PostgreSqlEnumType: "notification_type"),
                new("post_id", ColumnKind.Guid),
                new("read", ColumnKind.Boolean, IsDatabaseGenerated: true),
                new("created_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),

            new TableDefinition(
                "device_tokens",
                new("id", ColumnKind.Guid, IsPrimaryKey: true, IsDatabaseGenerated: true),
                new("user_id", ColumnKind.Guid),
                new("token", ColumnKind.String),
                new("platform", ColumnKind.String, PostgreSqlEnumType: "platform_type"),
                new("updated_at", ColumnKind.DateTime, IsDatabaseGenerated: true)),
        };

        _tables = tables.ToDictionary(table => table.Name, StringComparer.OrdinalIgnoreCase);
    }

    public bool TryGetTable(string tableName, out TableDefinition table) =>
        _tables.TryGetValue(NormalizeTableName(tableName), out table!);

    public IReadOnlyCollection<string> TableNames => _tables.Keys;

    private static string NormalizeTableName(string tableName) =>
        tableName.Replace("-", "_", StringComparison.Ordinal).Trim();
}

