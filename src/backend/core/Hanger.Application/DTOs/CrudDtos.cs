using System.Text.Json;

namespace Hanger.Application.DTOs;

public sealed record TableRecordDto(IReadOnlyDictionary<string, object?> Values);

public sealed record CreateRecordRequest(Dictionary<string, JsonElement> Values);

public sealed record UpdateRecordRequest(Dictionary<string, JsonElement> Values);

