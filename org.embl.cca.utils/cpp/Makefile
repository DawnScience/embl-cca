include Makefile.shared

MAKECPP := Makefile.cppsrc

.PHONY: all all32 all64
all : all$(OSBITS)
all32 all64 :
	$(MAKE) -f $(MAKECPP) $@

.PHONY: clean
#Clean files that make produces
clean :
	$(MAKE) -f $(MAKECPP) clean

.PHONY: empty
#Clean files that configure produces (that is not make)
#Not defined currently, since we do not use configure
empty :
#	$(MAKE) -f $(MAKECPP) empty

.PHONY: distclean
distclean : clean empty
