PHONY: docs graphs
graphs:
	for file in $$(ls docs/*.dot); do \
		echo $${file}; \
		dot2tex --autosize --pgf210 --figonly --texmode raw $${file} > $$(echo $${file} | cut -d'.' -f1).tex; \
	done

docs: graphs
	cd docs; xelatex doc.tex && xelatex doc.tex

