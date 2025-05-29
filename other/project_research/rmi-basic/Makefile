FIND := $(shell which gfind 2>/dev/null || which find)
JAVA  := $(shell $(FIND) src -type f -name *.java)
CLASS := $(patsubst src/%.java, build/%.class, $(JAVA))

JAVACOPT := -Xlint:deprecation

all: build/.create $(CLASS)
build/.create:
	@mkdir -p build
	@touch $@

$(CLASS): build/%.class: src/%.java
	@echo "[JAVAC] $<"
	@javac $(JAVACOPT) -cp 'src' $< -d build

.PHONY: clean
clean:
	@echo "[RM] build"
	@rm -rf build
