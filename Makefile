VERSION := $(shell grep __version__ py_ibm_4610/version.py | cut -d "=" -f 2 | tr -d "' _")
PWD := $(shell pwd)
BUILD_DIR = $(PWD)/dist
PROG := py_ibm_4610-$(VERSION)-py3-none-any.whl

.PHONY: all version build clean install

build: $(BUILD_DIR) $(BUILD_DIR)/$(PROG)
	@echo "Last steps"

$(BUILD_DIR)/$(PROG):
	@echo target is $@
	python3 -m build
	ls $(BUILD_DIR)/$(PROG) && echo "$(BUILD_DIR)/$(PROG)" >> .build_artifacts

install:
	python3 -m pip install --upgrade --user "$(BUILD_DIR)/$(PROG)"

test:
	python3 -m pytest

version:
	@echo "$(VERSION)"

clean:
	[ -f "$(BUILD_DIR)/$(PROG)" ] && rm -vf "$(BUILD_DIR)/$(PROG)" || :

$(BUILD_DIR):
	mkdir -p "$(BUILD_DIR)"
