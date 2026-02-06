import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import MessageBubble from './MessageBubble.vue'

describe('MessageBubble markdown rendering', () => {
  it('renders assistant content as markdown', () => {
    const wrapper = mount(MessageBubble, {
      props: {
        type: 'assistant',
        content: '# Title\n\n**bold** text\n\n- one\n- two\n\n`code`'
      }
    })

    const html = wrapper.html()
    expect(html).toContain('<h1>Title</h1>')
    expect(html).toContain('<strong>bold</strong>')
    expect(html).toContain('<li>one</li>')
    expect(html).toContain('<code>code</code>')
  })

  it('escapes unsafe html in markdown', () => {
    const wrapper = mount(MessageBubble, {
      props: {
        type: 'assistant',
        content: '<script>alert("x")</script>\n\n**safe**'
      }
    })

    const html = wrapper.html()
    expect(html).not.toContain('<script>alert("x")</script>')
    expect(html).toContain('&lt;script&gt;alert("x")&lt;/script&gt;')
    expect(html).toContain('<strong>safe</strong>')
  })
})
