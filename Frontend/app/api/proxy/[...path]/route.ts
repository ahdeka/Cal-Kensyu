import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  return handleProxy(request, path);
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  return handleProxy(request, path);
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  return handleProxy(request, path);
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  return handleProxy(request, path);
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  return handleProxy(request, path);
}

async function handleProxy(request: NextRequest, pathArray: string[]) {
  const backendUrl = process.env.BACKEND_URL || 'http://13.125.237.133:8080';
  
  // Reconstruct the path
  const path = '/' + pathArray.join('/');
  
  // Extract query string
  const url = new URL(request.url);
  const queryString = url.search;
  
  const targetUrl = `${backendUrl}${path}${queryString}`;
  
  console.log('Proxying to:', targetUrl);
  
  try {
    const headers: HeadersInit = {};
    
    // Forward relevant headers
    const forwardHeaders = ['content-type', 'authorization', 'cookie', 'accept'];
    forwardHeaders.forEach(header => {
      const value = request.headers.get(header);
      if (value) {
        headers[header] = value;
      }
    });
    
    // Get body if exists
    let body = undefined;
    if (request.method !== 'GET' && request.method !== 'HEAD') {
      body = await request.text();
    }
    
    const response = await fetch(targetUrl, {
      method: request.method,
      headers,
      body,
    });
    
    const contentType = response.headers.get('content-type');
    let data;
    
    if (contentType?.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }
    
    // Create response
    const nextResponse = NextResponse.json(data, {
      status: response.status,
    });
    
    // Forward Set-Cookie headers
    const setCookieHeaders = response.headers.getSetCookie();
    setCookieHeaders.forEach(cookie => {
      // Parse cookie to modify attributes
      const cookieParts = cookie.split(';').map(part => part.trim());
      const cookieValue = cookieParts[0];
      
      // Remove Secure and SameSite=None for development
      const modifiedCookie = cookieParts
        .filter(part => 
          !part.toLowerCase().startsWith('secure') &&
          !part.toLowerCase().startsWith('samesite=none')
        )
        .join('; ');
      
      nextResponse.headers.append('Set-Cookie', modifiedCookie);
    });
    
    // Forward other headers
    response.headers.forEach((value, key) => {
      if (!['content-encoding', 'transfer-encoding', 'connection', 'set-cookie'].includes(key.toLowerCase())) {
        nextResponse.headers.set(key, value);
      }
    });
    
    return nextResponse;
  } catch (error) {
    console.error('Proxy error:', error);
    return NextResponse.json(
      { error: 'Proxy request failed', details: String(error) },
      { status: 500 }
    );
  }
}