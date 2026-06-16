using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class CategoriesRepository(NpgsqlDataSource dataSource) : ICategoriesRepository
{
    // DTO interno para o Dapper mapear apenas as colunas id e name da tabela categories
    private sealed record CategoryRow(int Id, string Name);

    public async Task<IReadOnlyList<CategoryDto>> GetAllAsync(CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string categoriesSql = "SELECT id, name FROM categories ORDER BY id";
        const string typesSql = """
            SELECT t.id, t.category_id, c.name AS category_name, t.name
            FROM types t
            JOIN categories c ON c.id = t.category_id
            ORDER BY t.category_id, t.id
            """;

        var rows = (await connection.QueryAsync<CategoryRow>(categoriesSql)).ToList();
        var types = (await connection.QueryAsync<TypeDto>(typesSql)).ToList();

        var typesByCategory = types
            .GroupBy(t => t.CategoryId)
            .ToDictionary(g => g.Key, g => (IReadOnlyList<TypeDto>)g.ToList());

        return rows
            .Select(r => new CategoryDto(r.Id, r.Name, typesByCategory.GetValueOrDefault(r.Id, Array.Empty<TypeDto>())))
            .ToList();
    }

    public async Task<CategoryDto?> GetByIdAsync(int id, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string catSql = "SELECT id, name FROM categories WHERE id = @Id";
        var row = await connection.QuerySingleOrDefaultAsync<CategoryRow>(catSql, new { Id = id });

        if (row is null) return null;

        var types = await GetTypesByCategoryAsync(id, cancellationToken);
        return new CategoryDto(row.Id, row.Name, types);
    }

    public async Task<IReadOnlyList<TypeDto>> GetTypesByCategoryAsync(int categoryId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT t.id, t.category_id, c.name AS category_name, t.name
            FROM types t
            JOIN categories c ON c.id = t.category_id
            WHERE t.category_id = @CategoryId
            ORDER BY t.id
            """;

        var result = await connection.QueryAsync<TypeDto>(sql, new { CategoryId = categoryId });
        return result.ToList();
    }

    public async Task<TypeDto?> GetTypeByIdAsync(int typeId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT t.id, t.category_id, c.name AS category_name, t.name
            FROM types t
            JOIN categories c ON c.id = t.category_id
            WHERE t.id = @TypeId
            """;

        return await connection.QuerySingleOrDefaultAsync<TypeDto>(sql, new { TypeId = typeId });
    }

    public async Task<CategoryDto> CreateCategoryAsync(CreateCategoryRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            INSERT INTO categories (name) VALUES (@Name)
            RETURNING id, name
            """;

        var row = await connection.QuerySingleAsync<CategoryRow>(sql, new { request.Name });
        return new CategoryDto(row.Id, row.Name, Array.Empty<TypeDto>());
    }

    public async Task<TypeDto> CreateTypeAsync(CreateTypeRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            INSERT INTO types (category_id, name) VALUES (@CategoryId, @Name)
            RETURNING id
            """;

        var typeId = await connection.ExecuteScalarAsync<int>(sql, new { request.CategoryId, request.Name });
        return (await GetTypeByIdAsync(typeId, cancellationToken))!;
    }

    public async Task<CategoryDto?> UpdateCategoryAsync(int id, UpdateCategoryRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            UPDATE categories SET name = @Name WHERE id = @Id
            RETURNING id, name
            """;

        var row = await connection.QuerySingleOrDefaultAsync<CategoryRow>(sql, new { Id = id, request.Name });
        if (row is null) return null;

        var types = await GetTypesByCategoryAsync(id, cancellationToken);
        return new CategoryDto(row.Id, row.Name, types);
    }

    public async Task<TypeDto?> UpdateTypeAsync(int typeId, UpdateTypeRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "UPDATE types SET name = @Name WHERE id = @TypeId";
        var affected = await connection.ExecuteAsync(sql, new { TypeId = typeId, request.Name });

        if (affected == 0) return null;
        return await GetTypeByIdAsync(typeId, cancellationToken);
    }

    public async Task<bool> DeleteCategoryAsync(int id, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM categories WHERE id = @Id";
        var affected = await connection.ExecuteAsync(sql, new { Id = id });
        return affected > 0;
    }

    public async Task<bool> DeleteTypeAsync(int typeId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM types WHERE id = @TypeId";
        var affected = await connection.ExecuteAsync(sql, new { TypeId = typeId });
        return affected > 0;
    }
}