import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

class GitManager {
    private Git git;
    private Repository repository;

    public GitManager() {
        initializeGit();
    }

    private void initializeGit() {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            repository = builder.setGitDir(new File(".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            git = new Git(repository);
        } catch (IOException e) {
            // Not in a git repository
            git = null;
            repository = null;
        }
    }

    public String getCurrentBranch() {
        if (repository == null) return null;

        try {
            return repository.getBranch();
        } catch (IOException e) {
            return null;
        }
    }

    public void showStatus() {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            Status status = git.status().call();

            System.out.println("=== Git Status ===");
            System.out.println("Branch: " + getCurrentBranch());

            if (!status.getAdded().isEmpty()) {
                System.out.println("\nStaged files:");
                status.getAdded().forEach(file -> System.out.println("  A " + file));
            }

            if (!status.getModified().isEmpty()) {
                System.out.println("\nModified files:");
                status.getModified().forEach(file -> System.out.println("  M " + file));
            }

            if (!status.getUntracked().isEmpty()) {
                System.out.println("\nUntracked files:");
                status.getUntracked().forEach(file -> System.out.println("  ? " + file));
            }

            if (status.isClean()) {
                System.out.println("Working directory clean");
            }

        } catch (GitAPIException e) {
            System.err.println("Error getting status: " + e.getMessage());
        }
    }

    public void addFiles(String[] files) {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            if (files.length == 0 || (files.length == 1 && files[0].equals("."))) {
                git.add().addFilepattern(".").call();
                System.out.println("Added all files");
            } else {
                for (String file : files) {
                    git.add().addFilepattern(file).call();
                    System.out.println("Added: " + file);
                }
            }
        } catch (GitAPIException e) {
            System.err.println("Error adding files: " + e.getMessage());
        }
    }

    public void commit(String message) {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        if (message.isEmpty()) {
            System.out.println("Commit message required");
            return;
        }

        try {
            RevCommit commit = git.commit().setMessage(message).call();
            System.out.println("Committed: " + commit.getShortMessage());
            System.out.println("SHA: " + commit.getId().abbreviate(7).name());
        } catch (GitAPIException e) {
            System.err.println("Error committing: " + e.getMessage());
        }
    }

    public void push() {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            git.push().call();
            System.out.println("Pushed to remote");
        } catch (GitAPIException e) {
            System.err.println("Error pushing: " + e.getMessage());
        }
    }

    public void pull() {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            git.pull().call();
            System.out.println("Pulled from remote");
        } catch (GitAPIException e) {
            System.err.println("Error pulling: " + e.getMessage());
        }
    }

    public void listBranches() {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            String currentBranch = getCurrentBranch();
            List<Ref> branches = git.branchList().call();

            System.out.println("Branches:");
            for (Ref branch : branches) {
                String name = branch.getName().replace("refs/heads/", "");
                String marker = name.equals(currentBranch) ? "* " : "  ";
                System.out.println(marker + name);
            }
        } catch (GitAPIException e) {
            System.err.println("Error listing branches: " + e.getMessage());
        }
    }

    public void createBranch(String branchName) {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            git.branchCreate().setName(branchName).call();
            System.out.println("Created branch: " + branchName);
        } catch (GitAPIException e) {
            System.err.println("Error creating branch: " + e.getMessage());
        }
    }

    public void deleteBranch(String branchName) {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            git.branchDelete().setBranchNames(branchName).call();
            System.out.println("Deleted branch: " + branchName);
        } catch (GitAPIException e) {
            System.err.println("Error deleting branch: " + e.getMessage());
        }
    }

    public void checkout(String branchName) {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        if (branchName.isEmpty()) {
            System.out.println("Branch name required");
            return;
        }

        try {
            git.checkout().setName(branchName).call();
            System.out.println("Switched to branch: " + branchName);
        } catch (GitAPIException e) {
            System.err.println("Error checking out branch: " + e.getMessage());
        }
    }

    public void showLog(int count) {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            Iterable<RevCommit> commits = git.log().setMaxCount(count).call();

            System.out.println("=== Commit Log ===");
            for (RevCommit commit : commits) {
                System.out.println("Commit: " + commit.getId().abbreviate(7).name());
                System.out.println("Author: " + commit.getAuthorIdent().getName());
                System.out.println("Date: " + new Date(commit.getCommitTime() * 1000L));
                System.out.println("Message: " + commit.getShortMessage());
                System.out.println();
            }
        } catch (GitAPIException e) {
            System.err.println("Error showing log: " + e.getMessage());
        }
    }

    public void initRepository() {
        try {
            Git.init().setDirectory(new File(".")).call();
            System.out.println("Initialized empty Git repository");
            // Re-initialize after creating repo
            initializeGit();
        } catch (GitAPIException e) {
            System.err.println("Error initializing repository: " + e.getMessage());
        }
    }

    public void showDiff() {
        if (git == null) {
            System.out.println("Not in a git repository");
            return;
        }

        try {
            // This is a simplified diff - in practice you'd want more sophisticated diff display
            Status status = git.status().call();

            if (status.getModified().isEmpty() && status.getAdded().isEmpty()) {
                System.out.println("No changes to show");
                return;
            }

            System.out.println("=== Changes ===");
            status.getModified().forEach(file -> System.out.println("Modified: " + file));
            status.getAdded().forEach(file -> System.out.println("Added: " + file));

        } catch (GitAPIException e) {
            System.err.println("Error showing diff: " + e.getMessage());
        }
    }
}
