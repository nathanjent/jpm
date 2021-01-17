package org.jpm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Project {
    private static final String MAIN_SOURCE_PATH = "src/main/java";
    private static final String GIT_META_PATH = ".git";
    private static final String MAIN_RESOURCE_PATH = "src/main/resources";
    private static final String BUILD_PATH = "build";
    private static final String LIB_PATH = "lib";

    /**
     * Find the project root by searching up the directory ancestry
     * for a path containing the {@link Project#MAIN_SOURCE_PATH}.
     * Falls back to using a path containing the .git metadata directory.
     */
    private Path calculateProjectPath() {
        var path = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        var root = path.getRoot();
        Path alternatePath = null;
        while (path != null && !root.equals(path)) {
            // Use the path containing the default source directory structure
            if (path.resolve(MAIN_SOURCE_PATH).toFile().exists()) {
                return path;
            } else {
                // Alternately use the path containing a Git repository
                var gitMetaDir = path.resolve(GIT_META_PATH).toFile(); 
                if (gitMetaDir.exists() && gitMetaDir.isDirectory()) {
                    alternatePath = path;
                }

                path = path.getParent();
            }
        }
        return alternatePath;
    }

    private Path _projectPath;
    private Path _sourcePath;
    private Path _resourcePath;
    private Path _buildPath;
    private Path _libPath;

    public Project() {
        _projectPath = calculateProjectPath();
        _sourcePath = _projectPath.resolve(MAIN_SOURCE_PATH);
        _resourcePath = _projectPath.resolve(MAIN_RESOURCE_PATH);
        _buildPath = _projectPath.resolve(BUILD_PATH);
        _libPath = _projectPath.resolve(LIB_PATH);
        _buildPath.toFile().mkdirs();
        _libPath.resolve("main").toFile().mkdirs();
        _libPath.resolve("transitive").toFile().mkdirs();
        _resourcePath.toFile().mkdirs();
    }

    public Path getProjectPath() {
        return _projectPath;
    }

    public Path getSourcePath() {
        return _sourcePath;
    }

    public Path getResourcePath() {
      return _resourcePath;
    }

    public Path getBuildPath() {
        return _buildPath;
    }

    public Path getLibraryPath() {
        return _libPath;
    }

    public Path getModulePath() {
        var sourcePath = getSourcePath();
        if (sourcePath == null) {
            System.err.println(
                    "A valid source path was not found.");
            System.exit(-1);
        }
        var sourceFile = sourcePath.toFile();
        if (sourceFile == null || !sourceFile.isDirectory()) {
            System.err.println(
                    "Path does not exist \"" +
                    MAIN_SOURCE_PATH +
                    "\" in project \"" +
                    _projectPath + "\"");
            System.exit(-1);
        }
        for (var file: sourceFile.listFiles()) {
            if (!file.getName().startsWith(".")) {
                return file.toPath();
            }
        }
        return null;
    }

    public String getProjectName() {
        return getModulePath().getFileName().toString();
    }

    private String _version;

    public String getProjectVersion() {
        if (_version != null) {
            return _version;
        }
        var regex = Pattern.compile(".*tag\\: v(\\d+\\.\\d+\\.\\d+).*");
        var result = Cmd.run("git log --simplify-by-decoration --decorate --pretty=oneline \"HEAD\"");
        var lines = result.split("\n");
        for (var line: lines) {
          var matcher = regex.matcher(line);
          if (matcher.matches()) {
              _version = matcher.group(1);
              return _version;
          }
        }
        _version = "1.0.0";
        return _version;
    }

    public String getProjectJarName() {
        return getProjectName() + "-" + getProjectVersion() + ".jar";
    }
}

