package io.jmix.plugin.spring.web;

import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.PluginInfo;
import io.jmix.plugin.core.PluginNotFoundException;
import io.jmix.plugin.core.PluginValidationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * REST API for managing plugins. Activated by default; can be disabled
 * with {@code jmix.plugin.rest.enabled=false}.
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>{@code GET    /api/plugins}             — list all registered plugins;</li>
 *   <li>{@code GET    /api/plugins/{id}}        — get a single plugin by identifier;</li>
 *   <li>{@code POST   /api/plugins/{id}/load}   — load the plugin;</li>
 *   <li>{@code POST   /api/plugins/{id}/unload} — unload the plugin;</li>
 *   <li>{@code POST   /api/plugins/upload}      — upload a plugin JAR and register it.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/plugins")
@ConditionalOnProperty(prefix = "jmix.plugin.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PluginRestController {

    private final JmixPluginManager manager;

    public PluginRestController(JmixPluginManager manager) {
        this.manager = manager;
    }

    @GetMapping
    public List<PluginInfoDto> list() {
        return manager.list().stream()
                .map(PluginInfoDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PluginInfoDto get(@PathVariable String id) throws PluginNotFoundException {
        PluginInfo info = manager.getInfo(id)
                .orElseThrow(() -> new PluginNotFoundException(id));
        return PluginInfoDto.from(info);
    }

    @PostMapping("/{id}/load")
    public PluginActionResponse load(@PathVariable String id,
                                     @RequestBody(required = false) LoadPluginRequest request) throws PluginException {
        if (request != null && !request.configuration().isEmpty()) {
            manager.get(id).ifPresent(plugin -> request.configuration().forEach((k, v) -> {
                if (plugin.getContext() != null) {
                    plugin.getContext().setConfig(k, v);
                }
            }));
        }
        manager.load(id);
        return PluginActionResponse.ok(id, "loaded");
    }

    @PostMapping("/{id}/unload")
    public PluginActionResponse unload(@PathVariable String id) throws PluginException {
        manager.unload(id);
        return PluginActionResponse.ok(id, "unloaded");
    }

    @PostMapping("/upload")
    public PluginInfoDto upload(@RequestParam("file") MultipartFile file) throws PluginException, IOException {
        if (file == null || file.isEmpty()) {
            throw new PluginValidationException(
                    io.jmix.plugin.core.ValidationResult.builder()
                            .error("file", "Plugin JAR file is required")
                            .build());
        }
        Path tmpDir = Files.createTempDirectory("jmix-plugin-upload-");
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "plugin.jar";
        }
        Path target = tmpDir.resolve(originalName);
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        var plugin = manager.registerFromJar(target);
        manager.load(plugin.getId());
        return PluginInfoDto.from(manager.getInfo(plugin.getId()).orElseThrow());
    }

    @ExceptionHandler(PluginNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PluginNotFoundException ex, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("plugin_not_found", ex.getMessage(), ex.getPluginId()));
    }

    @ExceptionHandler(PluginValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(PluginValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("plugin_validation_failed", ex.getMessage(), null));
    }

    @ExceptionHandler(PluginException.class)
    public ResponseEntity<ErrorResponse> handleGeneric(PluginException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("plugin_error", ex.getMessage(), null));
    }
}
