# Testing Guide for AI CLI Assistant

## How to Run

```bash
mvn exec:java -Dexec.mainClass="com.thunder.ai.AI_CLI_Assistant"
```

Or simply:
```bash
mvn exec:java
```

## Test Scenarios

### 1. File Operations
- ✅ `create a file apple.txt` → Should generate `touch apple.txt`
- `list all files in current directory` → Should generate `ls -la`
- `show me files ending with .txt` → Should generate `ls *.txt`
- `create a directory called test` → Should generate `mkdir test`
- `delete the file apple.txt` → Should generate `rm apple.txt`
- `copy apple.txt to banana.txt` → Should generate `cp apple.txt banana.txt`

### 2. System Information
- `show current directory` → Should generate `pwd`
- `show disk usage` → Should generate `df -h`
- `show memory usage` → Should generate `top -l 1 | head -n 10` or `vm_stat`
- `show current date and time` → Should generate `date`
- `show system information` → Should generate `uname -a`

### 3. Text Processing
- `count lines in apple.txt` → Should generate `wc -l apple.txt`
- `show first 10 lines of apple.txt` → Should generate `head -n 10 apple.txt`
- `search for word "test" in apple.txt` → Should generate `grep "test" apple.txt`
- `show content of apple.txt` → Should generate `cat apple.txt`

### 4. Network Operations
- `check if google.com is reachable` → Should generate `ping -c 4 google.com`
- `show network connections` → Should generate `netstat -an` or `lsof -i`
- `show my IP address` → Should generate `ifconfig | grep "inet "` or `ipconfig getifaddr en0`

### 5. Process Management
- `show running processes` → Should generate `ps aux`
- `find process by name java` → Should generate `ps aux | grep java`
- `show top processes by CPU` → Should generate `top -l 1 | head -n 20`

### 6. Dangerous Commands (Should be Blocked)
- `delete everything` → Should be blocked (contains "rm -rf")
- `remove all files` → Should be blocked
- `shutdown the system` → Should be blocked
- `sudo rm -rf /` → Should be blocked

### 7. Complex Commands
- `find all .java files in current directory` → Should generate `find . -name "*.java"`
- `compress apple.txt into apple.zip` → Should generate `zip apple.zip apple.txt`
- `show environment variables` → Should generate `env`
- `change to home directory` → Should generate `cd ~`

### 8. Edge Cases
- Empty input → Should handle gracefully
- `exit` → Should exit the program
- Very long input → Should handle appropriately
- Special characters in input → Should be handled

## Expected Behavior

1. **Command Generation**: The AI should generate appropriate macOS/Unix shell commands
2. **Safety Check**: Dangerous commands should be blocked with "❌ Dangerous command! Blocked."
3. **User Confirmation**: Always asks for confirmation before running commands
4. **Error Handling**: Should handle API errors gracefully
5. **Output Display**: Should show command output clearly

## Testing Checklist

- [ ] Basic file operations work
- [ ] System information commands work
- [ ] Dangerous commands are blocked
- [ ] User can exit cleanly
- [ ] Error messages are clear
- [ ] Command output is displayed correctly
- [ ] Multiple commands in sequence work
- [ ] Special characters are handled

## Quick Test Script

You can create a test file with commands to try:

```bash
# Test file operations
create a file test.txt
list all files
show content of test.txt
delete test.txt

# Test system info
show current directory
show current date

# Test dangerous commands (should be blocked)
rm -rf /
sudo shutdown now

# Exit
exit
```


