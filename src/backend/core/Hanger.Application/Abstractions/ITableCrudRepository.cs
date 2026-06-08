using System.Text.Json;

namespace Hanger.Application.Abstractions;

public interface ITableCrudRepository
{
    Task<IReadOnlyList<IReadOnlyDictionary<string, object?>>> GetAllAsync(string tableName, CancellationToken cancellationToken);

    Task<IReadOnlyDictionary<string, object?>?> GetByKeyAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        CancellationToken cancellationToken);

    Task<IReadOnlyDictionary<string, object?>> CreateAsync(
        string tableName,
        IReadOnlyDictionary<string, JsonElement> values,
        CancellationToken cancellationToken);

    Task<IReadOnlyDictionary<string, object?>?> UpdateAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        IReadOnlyDictionary<string, JsonElement> values,
        CancellationToken cancellationToken);

    Task<bool> DeleteAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        CancellationToken cancellationToken);
}

