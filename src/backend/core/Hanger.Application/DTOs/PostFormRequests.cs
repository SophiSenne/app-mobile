using Microsoft.AspNetCore.Http;

namespace Hanger.Application.DTOs;

/// <summary>
/// Dados para criação de um post recebidos via multipart/form-data.
/// O campo Image é obrigatório; o upload para o Supabase Storage
/// é feito pelo controller antes de chamar o service.
/// </summary>
public sealed class CreatePostFormRequest
{
    /// <summary>Arquivo de imagem (obrigatório).</summary>
    public IFormFile? Image { get; set; }

    public string? Title { get; set; }
    public string? Caption { get; set; }
    public string? WeatherCondition { get; set; }
    public double? Temperature { get; set; }
    public string? City { get; set; }
    public int? CategoryId { get; set; }
    public int? TypeId { get; set; }
}

/// <summary>
/// Dados para atualização de um post recebidos via multipart/form-data.
/// O campo Image é opcional; se omitido, a imagem atual é mantida.
/// </summary>
public sealed class UpdatePostFormRequest
{
    /// <summary>Nova imagem (opcional — se omitida, mantém a atual).</summary>
    public IFormFile? Image { get; set; }

    public string? Title { get; set; }
    public string? Caption { get; set; }
    public string? WeatherCondition { get; set; }
    public double? Temperature { get; set; }
    public string? City { get; set; }
    public int? CategoryId { get; set; }
    public int? TypeId { get; set; }
}