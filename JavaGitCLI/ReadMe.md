Git CLI Workflow Automation Tool
A powerful Java-based command-line interface tool that automates Git workflows and terminal operations using JGit and ProcessBuilder. Features include fuzzy search, command history, branch management, and alias support for enhanced navigation and Git automation.
🚀 Features

Git Integration: Full Git operations using JGit library
Command History: Persistent command history with search capabilities
Fuzzy Search: Smart search through command history
Branch Management: Easy branch creation, deletion, and switching
Alias Support: Create custom command aliases
System Commands: Execute system commands through ProcessBuilder
Interactive CLI: User-friendly command-line interface

📋 Prerequisites

Java 11 or higher
Maven 3.6 or higher
Git installed on your system

🔧 Setup and Installation

Clone or create the project:
bashmkdir git-cli-tool
cd git-cli-tool

Create the project structure:
git-cli-tool/
├── src/
│   └── main/
│       └── java/
│           └── GitCLITool.java
├── pom.xml
└── README.md

Build the project:
bashmvn clean compile package

Run the tool:
bashjava -jar target/git-cli-tool.jar
Or directly with Maven:
bashmvn exec:java -Dexec.mainClass="GitCLITool"


🖥️ Usage
Starting the Tool
When you start the tool, you'll see a welcome message and a prompt showing the current Git branch (if in a Git repository):
╔═══════════════════════════════════════╗
║     Git CLI Workflow Automation      ║
║            Version 1.0               ║
╚═══════════════════════════════════════╝

Type 'help' for available commands
Type 'exit' to quit

[main] >
Git Commands
CommandAliasDescriptionstatusstShow Git statusadd <files>Add files to staging areacommit <message>ciCommit changes with messagepushPush changes to remotepullPull changes from remotebranchbrList all branchesbranch create <name>Create new branchbranch delete <name>Delete branchcheckout <branch>coSwitch to branchlog [count]Show commit historydiffShow file differences
System Commands
CommandDescriptionexec <command>Execute system commandpwdShow current directorycd <path>Change directorylsList files in current directoryclearClear screen
Tool Commands
CommandDescriptionhistory or histShow command historysearch <query>Fuzzy search through historyaliasList all aliasesalias <name> <command>Create new aliashelpShow help messageexit or quitExit the tool
💡 Example Usage
Basic Git Workflow
bash[main] > status
=== Git Status ===
Branch: main
Modified files:
M src/GitCLITool.java

[main] > add .
Added all files

[main] > commit "Updated CLI tool with new features"
Committed: Updated CLI tool with new features
SHA: a1b2c3d

[main] > push
Pushed to remote
Branch Management
bash[main] > branch
Branches:
* main
  feature-branch

[main] > branch create new-feature
Created branch: new-feature

[main] > checkout new-feature
Switched to branch: new-feature

[new-feature] >
Using Aliases
bash[main] > alias gs "git status"
Alias created: gs -> git status

[main] > alias
Defined aliases:
gs -> git status

[main] > gs
=== Git Status ===
Branch: main
Working directory clean
Command History and Search
bash[main] > history
Command History:
1. status
2. add .
3. commit "Initial commit"
4. push

[main] > search commit
Search results:
1. commit "Initial commit"
2. commit "Updated CLI tool"
   System Commands
   bash[main] > pwd
   /home/user/projects/git-cli-tool

[main] > ls
[DIR]  src
[DIR]  target
[FILE] pom.xml
[FILE] README.md

[main] > exec echo "Hello World"
Hello World
🔧 Configuration
The tool creates configuration files in your home directory:

~/.gitcli_history - Command history
~/.gitcli_aliases - Custom aliases
~/.gitcli/ - Configuration directory

🏗️ Architecture
The tool is structured with several key components:

GitCLITool: Main application class handling command processing
CommandHistory: Manages persistent command history
AliasManager: Handles custom command aliases
GitManager: Wraps JGit operations for Git functionality
FuzzySearcher: Implements fuzzy search algorithm

🚀 Advanced Features
Fuzzy Search Algorithm
The fuzzy search allows partial matching of commands:

Type comm to find commit commands
Search for br cr to find branch create commands

ProcessBuilder Integration
System commands are executed using ProcessBuilder:

Full process control with exit codes
Inherits IO for interactive commands
Cross-platform compatibility

JGit Integration
Direct Git repository manipulation:

No external Git dependencies
Pure Java implementation
Full Git feature support

🛠️ Development
Building from Source
bash# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Clean build
mvn clean package
Adding New Commands

Add case in processCommand() method
Implement command logic
Update help text in showHelp()
Add to README documentation

🤝 Contributing

Fork the repository
Create a feature branch
Make your changes
Add tests if applicable
Submit a pull request

📝 License
This project is licensed under the MIT License - see the LICENSE file for details.
🐛 Troubleshooting
Common Issues
"Not in a git repository" error:

Ensure you're in a directory with a .git folder
Initialize a repository with git init if needed

Command not found:

Check the help command for available commands
Verify command spelling and syntax

Permission denied on system commands:

Some system commands may require elevated privileges
Check file permissions and user access

Debug Mode
Set the logging level for more verbose output:
bashjava -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar target/git-cli-tool.jar
📞 Support
For issues and questions:

Check the troubleshooting section
Review command help with help
Check logs for error messages
Create an issue with detailed information