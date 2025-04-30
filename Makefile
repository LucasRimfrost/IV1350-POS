# Makefile för maven-projektet

# Variabler
MVN := mvn
JAVA := java
MAIN_CLASS := se.kth.iv1350.pos.startup.Main
JAR_FILE := target/pos-system-*.jar
MAIN_CLASS_FILE := target/classes/$(shell echo $(MAIN_CLASS) | tr . /).class

# ANSI färgkoder
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m

# Mål
.PHONY: all compile package run run-jar test clean install dependency-tree help

all: run

# Kompilera bara om klassfilen inte finns eller om källkoden är nyare
compile: $(MAIN_CLASS_FILE)

# Regel för att skapa klassfilen - körs bara om källkoden är nyare
$(MAIN_CLASS_FILE): $(shell find src/main/java -name "*.java")
	@echo "$(GREEN)Kompilerar projekt...$(NC)"
	@$(MVN) compile

package: $(MAIN_CLASS_FILE)
	@echo "$(GREEN)Paketerar projekt...$(NC)"
	@$(MVN) package

# Kör programmet - kompilerar bara om nödvändigt
run: $(MAIN_CLASS_FILE)
	@echo "$(GREEN)Kör programmet...$(NC)"
	@$(JAVA) -cp target/classes $(MAIN_CLASS)

run-jar: package
	@echo "$(GREEN)Kör från jar...$(NC)"
	@$(JAVA) -jar $(JAR_FILE)

test:
	@echo "$(GREEN)Kör tester...$(NC)"
	@$(MVN) test

clean:
	@echo "$(GREEN)Rensar projekt...$(NC)"
	@$(MVN) clean

install: $(MAIN_CLASS_FILE)
	@echo "$(GREEN)Installerar till lokalt repository...$(NC)"
	@$(MVN) install

dependency-tree:
	@echo "$(GREEN)Visar beroende-träd...$(NC)"
	@$(MVN) dependency:tree

help:
	@echo "$(YELLOW)Tillgängliga mål:$(NC)"
	@echo "  all            - Kompilera (om nödvändigt) och kör applikationen"
	@echo "  compile        - Kompilera källkoden (endast om nödvändigt)"
	@echo "  package        - Paketera den kompilerade koden till en JAR-fil"
	@echo "  run            - Kör applikationen (kompilerar först om nödvändigt)"
	@echo "  run-jar        - Kör applikationen från den paketerade JAR-filen"
	@echo "  test           - Kör tester"
	@echo "  clean          - Rensa projektet"
	@echo "  install        - Installera paketet till det lokala repositoriet"
	@echo "  dependency-tree - Visa beroendeträdet"
