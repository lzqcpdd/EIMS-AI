/** 下载后端返回的文件流（绕过 JSON 拦截器） */

import { API_BASE } from '@/utils/env'

export async function downloadFile(path, filename) {

  const token = localStorage.getItem('token')

  const res = await fetch(`${API_BASE}${path}`, {

    headers: token ? { Authorization: `Bearer ${token}` } : {}

  })

  const contentType = res.headers.get('content-type') || ''

  if (!res.ok || contentType.includes('application/json')) {

    const text = await res.text()

    let msg = '下载失败'

    try {

      const json = JSON.parse(text)

      msg = json.message || msg

    } catch {

      if (text) msg = text

    }

    throw new Error(msg)

  }

  const blob = await res.blob()

  const url = URL.createObjectURL(blob)

  const a = document.createElement('a')

  a.href = url

  a.download = filename

  document.body.appendChild(a)

  a.click()

  a.remove()

  URL.revokeObjectURL(url)

}


