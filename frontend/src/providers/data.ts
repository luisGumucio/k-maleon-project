import { DataProvider } from "@refinedev/core";
import { API_URL } from "./constants";

export const apiUrl = `${API_URL}/api`;

export async function fetchJson(url: string, options?: RequestInit) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || response.statusText);
  }
  if (response.status === 204) return null;
  return response.json();
}

export const dataProvider: DataProvider = {
  getList: async ({ resource, filters, sorters, pagination }) => {
    const params = new URLSearchParams();

    if (filters) {
      for (const filter of filters) {
        if ("field" in filter && filter.value !== undefined && filter.value !== "") {
          params.append(filter.field, String(filter.value));
        }
      }
    }

    if (pagination) {
      const page = (pagination as { current?: number }).current ?? 1;
      const size = pagination.pageSize ?? 20;
      params.append("page", String(page - 1));
      params.append("size", String(size));
    }

    if (sorters && sorters.length > 0) {
      params.append("sort", `${sorters[0].field},${sorters[0].order}`);
    }

    const query = params.toString() ? `?${params.toString()}` : "";
    const data = await fetchJson(`${apiUrl}/${resource}${query}`);

    if (Array.isArray(data)) {
      return { data, total: data.length };
    }
    return { data: data.content ?? data, total: data.totalElements ?? (data.content?.length ?? 0) };
  },

  getOne: async ({ resource, id }) => {
    const data = await fetchJson(`${apiUrl}/${resource}/${id}`);
    return { data };
  },

  create: async ({ resource, variables }) => {
    const data = await fetchJson(`${apiUrl}/${resource}`, {
      method: "POST",
      body: JSON.stringify(variables),
    });
    return { data };
  },

  update: async ({ resource, id, variables }) => {
    const data = await fetchJson(`${apiUrl}/${resource}/${id}`, {
      method: "PUT",
      body: JSON.stringify(variables),
    });
    return { data };
  },

  deleteOne: async ({ resource, id }) => {
    const data = await fetchJson(`${apiUrl}/${resource}/${id}`, {
      method: "DELETE",
    });
    return { data };
  },

  getApiUrl: () => apiUrl,
};
