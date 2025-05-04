# Simplified Makefile for Maven project
# Variables
MVN := mvn
JAVA := java
MAIN_CLASS := se.kth.iv1350.pos.startup.Main
MAIN_CLASS_FILE := target/classes/$(shell echo $(MAIN_CLASS) | tr . /).class

# ANSI color codes
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m

# Targets
.PHONY: all compile run test clean help

all: run

# Compile only if main class file doesn't exist or source code is newer
compile: $(MAIN_CLASS_FILE)

# Rule to create the class file - only runs if source code is newer
$(MAIN_CLASS_FILE): $(shell find src/main/java -name "*.java")
	@echo "$(GREEN)Compiling project...$(NC)"
	@$(MVN) compile

# Run the program - compiles first if necessary
run: $(MAIN_CLASS_FILE)
	@echo "$(GREEN)Running program...$(NC)"
	@$(JAVA) -cp target/classes $(MAIN_CLASS)

# Run tests
test:
	@echo "$(GREEN)Running tests...$(NC)"
	@$(MVN) test

# Clean the project
clean:
	@echo "$(GREEN)Cleaning project...$(NC)"
	@$(MVN) clean

# Help information
help:
	@echo "$(YELLOW)Available targets:$(NC)"
	@echo "  all      - Compile (if necessary) and run the application"
	@echo "  compile  - Compile source code (only if necessary)"
	@echo "  run      - Run the application (compiles first if necessary)"
	@echo "  test     - Run tests"
	@echo "  clean    - Clean the project"
