import sys

content = open(sys.argv[1]).read()
c = 0
for i, line in enumerate(content.split('\n'), 1):
    c += line.count('{') - line.count('}')
    if c < 0:
        print(f"Negative at line {i}: {line}")
print(f"Final count: {c}")
