using System.Text.Json;
using Hanger.Application.Abstractions;

namespace Hanger.Application.Services;

public interface ITableCrudService
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

public sealed class TableCrudService(ITableCrudRepository repository) : ITableCrudService
{
    public Task<IReadOnlyList<IReadOnlyDictionary<string, object?>>> GetAllAsync(
        string tableName,
        CancellationToken cancellationToken) =>
        repository.GetAllAsync(tableName, cancellationToken);

    public Task<IReadOnlyDictionary<string, object?>?> GetByKeyAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        CancellationToken cancellationToken) =>
        repository.GetByKeyAsync(tableName, keyValues, cancellationToken);

    public Task<IReadOnlyDictionary<string, object?>> CreateAsync(
        string tableName,
        IReadOnlyDictionary<string, JsonElement> values,
        CancellationToken cancellationToken) =>
        repository.CreateAsync(tableName, values, cancellationToken);

    public Task<IReadOnlyDictionary<string, object?>?> UpdateAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        IReadOnlyDictionary<string, JsonElement> values,
        CancellationToken cancellationToken) =>
        repository.UpdateAsync(tableName, keyValues, values, cancellationToken);

    public Task<bool> DeleteAsync(
        string tableName,
        IReadOnlyDictionary<string, string> keyValues,
        CancellationToken cancellationToken) =>
        repository.DeleteAsync(tableName, keyValues, cancellationToken);
}

