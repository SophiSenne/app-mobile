using System.Text.Json;
using Hanger.Application.Services;
using Hanger.Infrastructure.Data;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("api/{tableName}")]
public sealed class CrudController(ITableCrudService service, HangerSchema schema) : ControllerBase
{
    [HttpGet]
    public async Task<IActionResult> GetAll(string tableName, CancellationToken cancellationToken)
    {
        try
        {
            var records = await service.GetAllAsync(tableName, cancellationToken);
            return Ok(records);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message, new Dictionary<string, object?>
            {
                ["tables"] = schema.TableNames
            });
        }
    }

    [HttpGet("{id}")]
    public async Task<IActionResult> GetById(string tableName, string id, CancellationToken cancellationToken)
    {
        if (!TryGetSinglePrimaryKey(tableName, id, out var keys, out var error))
        {
            return BadRequestProblem(error);
        }

        return await GetByKeys(tableName, keys, cancellationToken);
    }

    [HttpGet("by-key")]
    public async Task<IActionResult> GetByCompositeKey(string tableName, CancellationToken cancellationToken) =>
        await GetByKeys(tableName, ReadQueryKeys(), cancellationToken);

    [HttpPost]
    public async Task<IActionResult> Create(
        string tableName,
        [FromBody] Dictionary<string, JsonElement> values,
        CancellationToken cancellationToken)
    {
        try
        {
            var created = await service.CreateAsync(tableName, values, cancellationToken);
            return CreatedAtAction(nameof(GetAll), new { tableName }, created);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateById(
        string tableName,
        string id,
        [FromBody] Dictionary<string, JsonElement> values,
        CancellationToken cancellationToken)
    {
        if (!TryGetSinglePrimaryKey(tableName, id, out var keys, out var error))
        {
            return BadRequestProblem(error);
        }

        return await UpdateByKeys(tableName, keys, values, cancellationToken);
    }

    [HttpPut("by-key")]
    public async Task<IActionResult> UpdateByCompositeKey(
        string tableName,
        [FromBody] Dictionary<string, JsonElement> values,
        CancellationToken cancellationToken) =>
        await UpdateByKeys(tableName, ReadQueryKeys(), values, cancellationToken);

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteById(string tableName, string id, CancellationToken cancellationToken)
    {
        if (!TryGetSinglePrimaryKey(tableName, id, out var keys, out var error))
        {
            return BadRequestProblem(error);
        }

        return await DeleteByKeys(tableName, keys, cancellationToken);
    }

    [HttpDelete("by-key")]
    public async Task<IActionResult> DeleteByCompositeKey(string tableName, CancellationToken cancellationToken) =>
        await DeleteByKeys(tableName, ReadQueryKeys(), cancellationToken);

    private async Task<IActionResult> GetByKeys(
        string tableName,
        IReadOnlyDictionary<string, string> keys,
        CancellationToken cancellationToken)
    {
        try
        {
            var record = await service.GetByKeyAsync(tableName, keys, cancellationToken);
            return record is null ? NotFoundProblem(tableName) : Ok(record);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    private async Task<IActionResult> UpdateByKeys(
        string tableName,
        IReadOnlyDictionary<string, string> keys,
        Dictionary<string, JsonElement> values,
        CancellationToken cancellationToken)
    {
        try
        {
            var updated = await service.UpdateAsync(tableName, keys, values, cancellationToken);
            return updated is null ? NotFoundProblem(tableName) : Ok(updated);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    private async Task<IActionResult> DeleteByKeys(
        string tableName,
        IReadOnlyDictionary<string, string> keys,
        CancellationToken cancellationToken)
    {
        try
        {
            var deleted = await service.DeleteAsync(tableName, keys, cancellationToken);
            return deleted ? NoContent() : NotFoundProblem(tableName);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    private bool TryGetSinglePrimaryKey(
        string tableName,
        string id,
        out IReadOnlyDictionary<string, string> keys,
        out string? error)
    {
        keys = new Dictionary<string, string>();
        error = null;

        if (!schema.TryGetTable(tableName, out var table))
        {
            error = $"Tabela '{tableName}' nao esta cadastrada para CRUD.";
            return false;
        }

        if (table.PrimaryKeys.Count != 1)
        {
            error = $"Tabela '{table.Name}' possui chave composta. Use /api/{table.Name}/by-key com query string.";
            return false;
        }

        keys = new Dictionary<string, string> { [table.PrimaryKeys[0].Name] = id };
        return true;
    }

    private Dictionary<string, string> ReadQueryKeys() =>
        Request.Query.ToDictionary(pair => pair.Key, pair => pair.Value.ToString(), StringComparer.OrdinalIgnoreCase);

    private IActionResult BadRequestProblem(string? detail, IDictionary<string, object?>? extensions = null) =>
        ProblemResponse(
            StatusCodes.Status400BadRequest,
            "Requisicao invalida.",
            detail ?? "Os parametros informados sao invalidos.",
            extensions);

    private IActionResult NotFoundProblem(string tableName) =>
        ProblemResponse(
            StatusCodes.Status404NotFound,
            "Registro nao encontrado.",
            $"Nenhum registro foi encontrado em '{tableName}' para a chave informada.");

    private IActionResult ProblemResponse(
        int statusCode,
        string title,
        string detail,
        IDictionary<string, object?>? extensions = null)
    {
        var problem = new ProblemDetails
        {
            Status = statusCode,
            Title = title,
            Detail = detail,
            Instance = HttpContext.Request.Path
        };

        if (extensions is not null)
        {
            foreach (var extension in extensions)
            {
                problem.Extensions[extension.Key] = extension.Value;
            }
        }

        return StatusCode(statusCode, problem);
    }
}
