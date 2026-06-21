using Hanger.Application.Abstractions;
using Microsoft.Extensions.Configuration;

namespace Hanger.Infrastructure.Repositories;

/// <summary>
/// Repositório que usa a API HTTP do Supabase Storage para gerenciar
/// imagens no bucket "images".
/// </summary>
public sealed class SupabaseStorageRepository : IStorageRepository
{
    private const string BucketName = "images";

    private readonly HttpClient _httpClient;
    private readonly string _publicBaseUrl;

    public SupabaseStorageRepository(HttpClient httpClient, IConfiguration configuration)
    {
        _httpClient = httpClient;

        // Lê do appsettings.json:
        //   "Supabase": {
        //     "Url":       "https://<project-ref>.supabase.co",
        //     "ServiceKey": "<service_role key>"
        //   }
        var supabaseUrl = configuration["Supabase:Url"]
            ?? throw new InvalidOperationException("Configuração 'Supabase:Url' não encontrada.");

        var serviceKey = configuration["Supabase:ServiceKey"]
            ?? throw new InvalidOperationException("Configuração 'Supabase:ServiceKey' não encontrada.");

        // Base URL para as chamadas à Storage API
        _httpClient.BaseAddress = new Uri($"{supabaseUrl.TrimEnd('/')}/storage/v1/");
        _httpClient.DefaultRequestHeaders.Add("Authorization", $"Bearer {serviceKey}");
        _httpClient.DefaultRequestHeaders.Add("apikey", serviceKey);

        // URL pública para montar o link de acesso após o upload
        _publicBaseUrl = $"{supabaseUrl.TrimEnd('/')}/storage/v1/object/public/{BucketName}";
    }

    /// <inheritdoc />
    public async Task<string> UploadImageAsync(
        Stream fileStream,
        string fileName,
        string contentType,
        string subfolder,
        CancellationToken cancellationToken = default)
    {
        // Gera um nome único para evitar colisões: <subfolder>/<guid>_<nome-original>
        var uniquePath = $"{subfolder}/{Guid.NewGuid()}_{SanitizeFileName(fileName)}";

        using var content = new StreamContent(fileStream);
        content.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(contentType);

        // POST /storage/v1/object/<bucket>/<path>
        var response = await _httpClient.PostAsync(
            $"object/{BucketName}/{uniquePath}",
            content,
            cancellationToken);

        if (!response.IsSuccessStatusCode)
        {
            var body = await response.Content.ReadAsStringAsync(cancellationToken);
            throw new InvalidOperationException(
                $"Falha ao fazer upload da imagem no Supabase Storage. " +
                $"Status: {(int)response.StatusCode}. Body: {body}");
        }

        // Retorna a URL pública
        return $"{_publicBaseUrl}/{uniquePath}";
    }

    /// <inheritdoc />
    public async Task DeleteImageAsync(string filePath, CancellationToken cancellationToken = default)
    {
        // O filePath pode ser a URL completa ou apenas o caminho relativo (posts/guid_nome.jpg)
        // Normaliza para obter somente o path relativo dentro do bucket
        var relativePath = ExtractRelativePath(filePath);

        // DELETE /storage/v1/object/<bucket>/<path>
        var response = await _httpClient.DeleteAsync(
            $"object/{BucketName}/{relativePath}",
            cancellationToken);

        // 404 significa que o arquivo já não existe — não tratamos como erro
        if (!response.IsSuccessStatusCode && response.StatusCode != System.Net.HttpStatusCode.NotFound)
        {
            var body = await response.Content.ReadAsStringAsync(cancellationToken);
            throw new InvalidOperationException(
                $"Falha ao deletar imagem no Supabase Storage. " +
                $"Status: {(int)response.StatusCode}. Body: {body}");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static string SanitizeFileName(string fileName)
    {
        // Remove espaços e caracteres problemáticos para a URL
        return Path.GetFileName(fileName)
            .Replace(" ", "_")
            .Replace("#", "")
            .Replace("?", "");
    }

    private string ExtractRelativePath(string filePath)
    {
        // Se for URL completa, extrai o path relativo após o nome do bucket
        var marker = $"/{BucketName}/";
        var idx = filePath.IndexOf(marker, StringComparison.Ordinal);
        return idx >= 0 ? filePath[(idx + marker.Length)..] : filePath;
    }
}
