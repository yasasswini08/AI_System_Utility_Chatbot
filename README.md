# 📊 AI System Utility – Smart Command-Line Assistant
### A Java-Based Intelligent System Utility Tool

An interactive AI-powered CLI assistant built in Java that simplifies system operations such as file handling, monitoring, text processing, and command generation.
It uses Google Gemini AI to convert natural language instructions into executable shell commands — safely and intelligently.

## 📁 Module Overview

Module	Description
AI_CLI_Assistant.java	- Main application entry point  - Handles user input and interaction  - Integrates with Google Gemini API for command generation  - Performs command safety checks  - Executes commands with user confirmation  - Includes model fallback mechanism for reliability
SSLUtil.java	- Utility class for SSL/TLS configuration  - Manages SSL certificate verification  - Provides SSL context for secure HTTP connections  - Handles trust managers for API communication

### ✅ Key Features
- 🤖 **Natural Language Processing**: Convert plain English instructions into shell commands
- 🛡️ **Safety First**: Built-in protection against dangerous commands (rm -rf, shutdown, etc.)
- ✅ **User Confirmation**: Always asks for confirmation before executing commands
- ⚡ **Performance Optimized**: HTTP client and API model caching for faster responses
- 🔄 **Model Fallback**: Automatically tries multiple Gemini API models for reliability
- 🔒 **SSL Support**: Handles SSL/TLS connections securely
- 🎯 **macOS Optimized**: Specifically designed for macOS/Unix shell environments
- 📝 **Interactive CLI**: Clean, user-friendly command-line interface

## Functionalities Included

### Command Generation
- Natural language to shell command conversion
- Support for file operations (create, delete, list, copy, move)
- System information queries (date, directory, disk usage, memory)
- Text processing commands (grep, wc, head, tail, cat)
- Network operations (ping, netstat, IP address)
- Process management (ps, top, kill)
- File searching and filtering

### Safety Features
- Dangerous command detection and blocking
- Blacklist of harmful command patterns
- User confirmation required before execution
- Error handling and graceful failure

### API Integration
- Google Gemini API integration
- Multiple model support (gemini-2.5-flash, gemini-2.0-flash, gemini-pro, etc.)
- Automatic model fallback mechanism
- API response caching for performance
- Error handling and retry logic


🛠️ Tools and Technologies Used

Technology	Version / Purpose
Java	17 — Core programming language
Maven	Build tool + dependency management
Google Gemini API	AI model used for intelligent command generation
Jackson	2.16.0 — JSON parsing & serialization
dotenv-java	3.0.0 — Environment variable handling
Java HTTP Client	Used for API communication (HTTP requests)
Maven Compiler Plugin	3.13.0 — Compiles Java source files
Maven Exec Plugin	3.1.0 — Runs the Java application through Maven

## How to Run

### Prerequisites

1. **Java 17 or higher** installed on your system
2. **Maven** installed and configured
3. **Google Gemini API Key** - Get your API key from [Google AI Studio](https://makersuite.google.com/app/apikey)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <https://github.com/yasasswini08/AI_System_Utility_Chatbot.git>
   cd java_chatbot
   ```

2. **Create a `.env` file** in the project root directory
   ```bash
   touch .env
   ```

3. **Add your Gemini API key** to the `.env` file
   ```
   GEMINI_API_KEY=your_api_key_here
   ```

4. **Build the project**
   ```bash
   mvn clean compile
   ```

5. **Run the application**
   ```bash
   mvn exec:java
   ```
   
   Or alternatively:
   ```bash
   mvn exec:java -Dexec.mainClass="com.thunder.ai.AI_CLI_Assistant"
   ```

   ### Usage Example

```
🤖 AI CLI Assistant (Java HTTP Version)

🧠 Ask something (or type exit): create a file test.txt

🔧 Suggested Command:
touch test.txt
Run this command? (yes/no): yes

🖥️ Output:successfully executed

🧠 Ask something (or type exit): list all files

🔧 Suggested Command:
ls -la
Run this command? (yes/no): yes

🖥️ Output:successfully executed
total 24
drwxr-xr-x  5 user  staff  160 Jan 15 10:30 .
drwxr-xr-x  8 user  staff  256 Jan 15 10:25 ..
-rw-r--r--  1 user  staff  123 Jan 15 10:30 test.txt
...

🧠 Ask something (or type exit): exit
👋 Bye my friend!
```
# Repository Structure

```
java_chatbot/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── thunder/
│   │               └── ai/
│   │                   ├── AI_CLI_Assistant.java    # Main application class
│   │                   └── SSLUtil.java             # SSL utility class
│   │
│   └── test/
│       └── java/                                    # Test directory
│
├── target/                                         # Compiled classes and build artifacts
│   └── classes/
│       └── com/
│           └── thunder/
│               └── ai/
│                   ├── AI_CLI_Assistant.class
│                   └── SSLUtil.class
│
├── pom.xml                                         # Maven project configuration
├── .env                                            # Environment variables (create this)
├── test_commands.txt                               # Sample test commands
├── TESTING.md                                      # Testing documentation
└── README.md                                       # Project documentation
```
# AI Chatbot Commands

The assistant supports a wide variety of natural language commands. Here are some examples organized by category:

| Category | Natural Language Command | Generated Shell Command |
|----------|-------------------------|------------------------|
| **File Operations** | `create a file test.txt` | `touch test.txt` |
| | `list all files` | `ls -la` |
| | `show files ending with .txt` | `ls *.txt` |
| | `create a directory called test` | `mkdir test` |
| | `delete the file test.txt` | `rm test.txt` |
| | `copy test.txt to backup.txt` | `cp test.txt backup.txt` |
| | `move test.txt to documents/` | `mv test.txt documents/` |
| **System Information** | `show current directory` | `pwd` |
| | `show disk usage` | `df -h` |
| | `show memory usage` | `vm_stat` |
| | `show current date and time` | `date` |
| | `show system information` | `uname -a` |
| **Text Processing** | `count lines in pom.xml` | `wc -l pom.xml` |
| | `show first 10 lines of file.txt` | `head -n 10 file.txt` |
| | `search for word "test" in file.txt` | `grep "test" file.txt` |
| | `show content of pom.xml` | `cat pom.xml` |
| | `show last 5 lines of file.txt` | `tail -n 5 file.txt` |
| **Network Operations** | `check if google.com is reachable` | `ping -c 4 google.com` |
| | `show network connections` | `netstat -an` |
| | `show my IP address` | `ifconfig \| grep "inet "` |
| **Process Management** | `show running processes` | `ps aux` |
| | `find process by name java` | `ps aux \| grep java` |
| | `show top processes by CPU` | `top -l 1 \| head -n 20` |
| **File Searching** | `find all .java files` | `find . -name "*.java"` |
| | `find all .txt files in current directory` | `find . -maxdepth 1 -name "*.txt"` |
| **Compression** | `compress test.txt into test.zip` | `zip test.zip test.txt` |
| **Environment** | `show environment variables` | `env` |
| | `change to home directory` | `cd ~` |
| **Dangerous Commands (Blocked)** | `delete everything` | ❌ Blocked |
| | `remove all files` | ❌ Blocked |
| | `shutdown the system` | ❌ Blocked |
| | `sudo rm -rf /` | ❌ Blocked |

## Safety Features

The assistant includes a built-in safety mechanism that blocks potentially dangerous commands:

- `rm -rf` (recursive force delete)
- `sudo rm` (privileged delete)
- `shutdown` (system shutdown)
- `reboot` (system reboot)
- `mkfs` (filesystem formatting)
- `dd if=` (disk operations)
- `kill -9 1` (killing critical processes)

When a dangerous command is detected, the assistant will display:
```
❌ Dangerous command! Blocked.
```

## Configuration

### Environment Variables

Create a `.env` file in the project root with the following:

```env
GEMINI_API_KEY=your_google_gemini_api_key_here
```

### Maven Configuration

The project uses Maven for dependency management. Key dependencies include:

- **dotenv-java**: For loading environment variables from `.env` file
- **jackson-databind**: For JSON parsing of API responses

## Troubleshooting

### Common Issues

1. **Missing API Key Error**
   ```
   ❌ Missing GEMINI_API_KEY in .env
   ```
   **Solution**: Ensure you have created a `.env` file with your `GEMINI_API_KEY`

2. **API Model Errors**
   ```
   ❌ All models failed. Last error: ...
   ```
   **Solution**: Check your API key validity and internet connection. The assistant will automatically try multiple models.

3. **SSL Verification Issues**
   - The application automatically disables SSL verification for development purposes
   - For production, consider implementing proper certificate validation

4. **Command Execution Errors**
   - Ensure you're running on macOS/Unix system
   - Check file permissions for the operations you're trying to perform
  
## 👨‍💻 Developed By
### **Yasaswini Idimukkala**
### 📧 [yasasswini.idimukkala.8@gmail.com]
### 🔗 [LINKEDIN](https://www.linkedin.com/in/yasasswini-idimukkala-749754367)





