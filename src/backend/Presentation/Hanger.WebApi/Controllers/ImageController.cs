using Hanger.Application.Abstractions;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

/// <summary>
/// Gerencia o upload de imagens para o Supabase Storage.
/// </summary>
[ApiController]
[Route("images")]
public sealed class ImageController(IImageService imageService) : ApiControllerBase
{
    private static readonly string[] AllowedImageTypes =
        ["image/jpeg", "image/png", "image/webp"];

    private const long MaxFileSizeBytes = 5 * 1024 * 1024; // 5 MB

    /// <summary>
    /// Faz upload de uma foto de perfil para a pasta "avatars" no Supabase Storage.
    /// Retorna a URL pública da imagem para ser usada como <c>avatarUrl</c> no cadastro
    /// ou na edição de perfil. Não requer autenticação — pode ser chamado antes do cadastro.
    /// </summary>
    /// <remarks>
    /// Enviar como <c>multipart/form-data</c> com o campo <c>file</c> contendo a imagem.
    /// Formatos aceitos: JPEG, PNG, WebP. Tamanho máximo: 5 MB.
    /// </remarks>
    [HttpPost("avatar")]
    [Consumes("multipart/form-data")]
    [ProducesResponseType<AvatarUploadResponse>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> UploadAvatar(
        IFormFile? file,
        CancellationToken cancellationToken)
    {
        if (file is null || file.Length == 0)
            return BadRequestProblem("Nenhum arquivo foi enviado. Use o campo 'file' no formulário.");

        if (!AllowedImageTypes.Contains(file.ContentType, StringComparer.OrdinalIgnoreCase))
            return BadRequestProblem(
                $"Tipo de arquivo não permitido. Use: {string.Join(", ", AllowedImageTypes)}.");

        if (file.Length > MaxFileSizeBytes)
            return BadRequestProblem("Arquivo muito grande. O limite é 5 MB.");

        await using var stream = file.OpenReadStream();

        var avatarUrl = await imageService.UploadAvatarAsync(
            stream,
            file.FileName,
            file.ContentType,
            cancellationToken);

        return Ok(new AvatarUploadResponse(avatarUrl));
    }
}

/// <summary>Resposta do endpoint de upload de foto de perfil.</summary>
public sealed record AvatarUploadResponse(string AvatarUrl);
