namespace Hanger.Application.Abstractions;

public interface IStorageRepository
{
    /// <summary>
    /// Faz upload de uma imagem para o bucket "images" do Supabase Storage
    /// e retorna a URL pública do arquivo.
    /// </summary>
    /// <param name="fileStream">Stream com o conteúdo da imagem.</param>
    /// <param name="fileName">Nome do arquivo, incluindo extensão (ex: "foto.jpg").</param>
    /// <param name="contentType">MIME type (ex: "image/jpeg").</param>
    /// <param name="subfolder">Subpasta dentro do bucket (ex: "posts", "avatars").</param>
    /// <param name="cancellationToken">Token de cancelamento.</param>
    /// <returns>URL pública da imagem no Supabase Storage.</returns>
    Task<string> UploadImageAsync(
        Stream fileStream,
        string fileName,
        string contentType,
        string subfolder,
        CancellationToken cancellationToken = default);

    /// <summary>
    /// Remove uma imagem do bucket "images" pelo caminho/nome do arquivo.
    /// </summary>
    Task DeleteImageAsync(string filePath, CancellationToken cancellationToken = default);
}
