#!/usr/bin/env python3
"""
Generate professional HTML documentation matching the original formatting
"""

import base64
import re

# Read logo and convert to base64
with open('/Users/skundu/Documents/Projects/coding-legion/logo.png', 'rb') as f:
    logo_data = base64.b64encode(f.read()).decode('utf-8')
    logo_base64 = f'data:image/png;base64,{logo_data}'

# Read the README (excluding the footer section at the end)
with open('/Users/skundu/Documents/Projects/coding-legion/README.md', 'r', encoding='utf-8') as f:
    all_lines = f.readlines()
    # Exclude lines after "## 📜 Copyright" (around line 978)
    markdown_lines = []
    skip = False
    for line in all_lines:
        if '## 📜 Copyright' in line:
            skip = True
        if not skip:
            markdown_lines.append(line)
    markdown_content = ''.join(markdown_lines)

# Convert markdown to HTML
def convert_markdown(md):
    lines = md.split('\n')
    html_lines = []
    in_code_block = False
    code_lang = ''
    in_list = False
    in_table = False
    table_rows = []
    
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Code blocks
        if line.startswith('```'):
            if not in_code_block:
                code_lang = line[3:].strip()
                in_code_block = True
                html_lines.append('<pre><code>')
            else:
                in_code_block = False
                html_lines.append('</code></pre>')
            i += 1
            continue
        
        if in_code_block:
            html_lines.append(line.replace('<', '&lt;').replace('>', '&gt;'))
            i += 1
            continue
        
        # Tables
        if '|' in line and line.strip().startswith('|'):
            if not in_table:
                in_table = True
                table_rows = []
            table_rows.append(line)
            i += 1
            if i < len(lines) and not ('|' in lines[i] and lines[i].strip().startswith('|')):
                # End of table
                html_lines.append(convert_table(table_rows))
                in_table = False
            continue
        
        # Lists
        if line.strip().startswith('- ') or line.strip().startswith('* '):
            if not in_list:
                html_lines.append('<ul>')
                in_list = True
            content = line.strip()[2:]
            # Process inline formatting
            content = process_inline(content)
            html_lines.append(f'<li>{content}</li>')
            i += 1
            if i < len(lines) and not (lines[i].strip().startswith('- ') or lines[i].strip().startswith('* ')):
                html_lines.append('</ul>')
                in_list = False
            continue
        
        # Close list if needed
        if in_list:
            html_lines.append('</ul>')
            in_list = False
        
        # Headers
        if line.startswith('#### '):
            html_lines.append(f'<h4>{process_inline(line[5:])}</h4>')
        elif line.startswith('### '):
            html_lines.append(f'<h3>{process_inline(line[4:])}</h3>')
        elif line.startswith('## '):
            html_lines.append(f'<h2>{process_inline(line[3:])}</h2>')
        elif line.startswith('# '):
            html_lines.append(f'<h1>{process_inline(line[2:])}</h1>')
        elif line.strip() == '---':
            html_lines.append('<hr>')
        elif line.strip() == '':
            html_lines.append('<p></p>')
        else:
            html_lines.append(f'<p>{process_inline(line)}</p>')
        
        i += 1
    
    if in_list:
        html_lines.append('</ul>')
    
    return '\n'.join(html_lines)

def process_inline(text):
    # Bold
    text = re.sub(r'\*\*([^*]+)\*\*', r'<strong>\1</strong>', text)
    # Italic
    text = re.sub(r'\*([^*]+)\*', r'<em>\1</em>', text)
    # Inline code
    text = re.sub(r'`([^`]+)`', r'<code>\1</code>', text)
    # Links
    text = re.sub(r'\[([^\]]+)\]\(([^)]+)\)', r'<a href="\2">\1</a>', text)
    return text

def convert_table(rows):
    html = '<table>\n'
    for idx, row in enumerate(rows):
        cells = [cell.strip() for cell in row.split('|') if cell.strip()]
        if idx == 0:
            html += '<tr>' + ''.join([f'<th>{process_inline(cell)}</th>' for cell in cells]) + '</tr>\n'
        elif idx == 1 and all(re.match(r'^[-:]+$', cell.replace(' ', '')) for cell in cells):
            continue  # Skip separator
        else:
            html += '<tr>' + ''.join([f'<td>{process_inline(cell)}</td>' for cell in cells]) + '</tr>\n'
    html += '</table>'
    return html

html_body = convert_markdown(markdown_content)

# Create HTML document with professional styling (matching original)
html_document = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coding Legion - Documentation</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');
        
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            line-height: 1.6;
            color: #1f2937;
            background: #ffffff;
            padding: 40px;
            max-width: 1200px;
            margin: 0 auto;
        }}
        
        .header {{
            text-align: center;
            margin-bottom: 60px;
            padding-bottom: 30px;
            border-bottom: 3px solid #e5e7eb;
        }}
        
        .logo {{
            width: 100px;
            height: 100px;
            margin: 0 auto 20px;
        }}
        
        .title {{
            font-size: 48px;
            font-weight: 700;
            color: #111827;
            margin-bottom: 10px;
        }}
        
        .subtitle {{
            font-size: 20px;
            color: #6b7280;
            font-weight: 400;
        }}
        
        h1 {{
            font-size: 36px;
            font-weight: 700;
            color: #111827;
            margin: 40px 0 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #e5e7eb;
        }}
        
        h2 {{
            font-size: 28px;
            font-weight: 600;
            color: #1f2937;
            margin: 35px 0 15px;
            padding-bottom: 8px;
            border-bottom: 1px solid #e5e7eb;
        }}
        
        h3 {{
            font-size: 22px;
            font-weight: 600;
            color: #374151;
            margin: 30px 0 15px;
        }}
        
        h4 {{
            font-size: 18px;
            font-weight: 600;
            color: #4b5563;
            margin: 25px 0 12px;
        }}
        
        p {{
            margin: 0 0 16px;
            color: #374151;
        }}
        
        code {{
            font-family: 'JetBrains Mono', 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace;
            background: #f3f4f6;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 14px;
            color: #dc2626;
        }}
        
        pre {{
            background: #1f2937;
            color: #e5e7eb;
            padding: 20px;
            border-radius: 8px;
            overflow-x: auto;
            margin: 20px 0;
            font-size: 14px;
            line-height: 1.5;
        }}
        
        pre code {{
            background: none;
            padding: 0;
            color: #e5e7eb;
        }}
        
        ul, ol {{
            margin: 0 0 16px 30px;
        }}
        
        li {{
            margin: 0 0 8px;
            color: #374151;
        }}
        
        strong {{
            font-weight: 600;
            color: #111827;
        }}
        
        em {{
            font-style: italic;
            color: #4b5563;
        }}
        
        a {{
            color: #2563eb;
            text-decoration: none;
            font-weight: 500;
        }}
        
        a:hover {{
            text-decoration: underline;
        }}
        
        hr {{
            border: none;
            border-top: 2px solid #e5e7eb;
            margin: 40px 0;
        }}
        
        blockquote {{
            border-left: 4px solid #3b82f6;
            padding-left: 20px;
            margin: 20px 0;
            color: #4b5563;
            font-style: italic;
        }}
        
        table {{
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }}
        
        th, td {{
            border: 1px solid #d1d5db;
            padding: 12px;
            text-align: left;
        }}
        
        th {{
            background: #f9fafb;
            font-weight: 600;
            color: #111827;
        }}
        
        .footer {{
            margin-top: 60px;
            padding-top: 30px;
            border-top: 2px solid #e5e7eb;
            text-align: center;
            color: #6b7280;
        }}
        
        @media print {{
            body {{
                padding: 20px;
            }}
            .header {{
                margin-bottom: 40px;
            }}
        }}
    </style>
</head>
<body>
    <div class="header">
        <img src="{logo_base64}" class="logo" alt="Coding Legion">
        <div class="title">Coding Legion</div>
        <div class="subtitle">Comprehensive Plugin Documentation</div>
    </div>
    
    <div class="content">
{html_body}
    </div>
    
    <div class="footer">
        <p><strong>© 2025 Saptarshi Kundu. All Rights Reserved.</strong></p>
        <p>Version 1.6.0</p>
    </div>
</body>
</html>
"""

# Write to output file
output_path = '/Users/skundu/Documents/personal_projects/Coding Legion/CodingLegion_Documentation.html'

with open(output_path, 'w', encoding='utf-8') as f:
    f.write(html_document)

print(f"✅ Professional HTML documentation created!")
print(f"📄 Location: {output_path}")
print(f"\n📥 To create PDF:")
print(f"1. Open the file in Safari or Chrome")
print(f"2. File → Export as PDF (Safari) or Print → Save as PDF (Chrome)")
print(f"3. Save as: CodingLegion_Documentation.pdf")

# Open in browser
import subprocess
subprocess.run(['open', output_path])
print(f"\n✅ Opening in browser...")

